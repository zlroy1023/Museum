package com.example.vlc;
import java.util.Vector;

public class Decoder_3B {
	// clk是系统中编码硬件使用的频率
	// data是MIC采样到的数据
	// fs是手机程序采用的采样频率
	// samples表示每个编码周期完全理想条件下能采样到的点的个数
	private int clk = 8000;
	private int[] data;
	private int fs;
	private int samples;

	// 构造函数
	Decoder_3B(int[] a, int b) {
		data = a;
		fs = b;
		samples = fs / clk;
	}

	// 求采样数据均值，用于判决高低电平
	private int avg(int[] data) {
		int length = data.length;
		int sum = 0;
		for (int i = 0; i < length; i++) {
			sum = sum + data[i];
		}
		sum = sum / length;
		return sum;
	}

	// 返回ID
	public long getID() {
		long id = decoder();
		return id;
	}

	// 解码主体函数
	public long decoder() {
		// 根据均值二值化，形成高低电平
		int avg = avg(data);
		for (int i = 0; i < data.length; i++) {
			if (data[i] >= avg)
				data[i] = 0;
			else
				data[i] = 1;
		}
//		System.out.println("data length = " + data.length);
//		for (int i = 0; i < data.length; i++) {
//		//	System.out.println(data[i]);
//		}
		// 得出电平发生跳变的位置，Machester码中电平跳变表征了1bit数据
		Vector<Integer> pos = new Vector<Integer>();
		for (int i = 0; i < data.length - 1; i++) {
			if (data[i] != data[i + 1])
				pos.add(i);
		}
	//	System.out.println("pos.size = " + pos.size());
		// 对于两个相邻跳变的位置，根据中间的数据得出实际发送的数据
		// 这个操作相当于降采样
		Vector<Integer> v = new Vector<Integer>();
		int flag = data[0];
		for (int i = 0; i < pos.size(); i++) {
			if (i == 0) {
				if (pos.elementAt(i) >= samples + 1) {
					v.add(flag);
					v.add(flag);
				} else
					v.add(flag);
			} else {
				if (pos.elementAt(i) - pos.elementAt(i - 1) >= samples + 3) {
					v.add(flag);
					v.add(flag);
				} else if (pos.elementAt(i) - pos.elementAt(i - 1) >= 2)
					v.add(flag);
				else
					return 0;
			}
			if (flag == 1)
				flag = 0;
			else
				flag = 1;
		}
//		for (int i = 0; i < v.size(); i = i + 1) {
//			System.out.println(v.elementAt(i));
//		}
		// 得到发送数据后，根据Manchester编码规则进行解码
		// 考虑到采样数据可能将某bit数据拆开了，因此需要判断直接解码和错位解码两种情形，用flag1表示
		int len = (int) v.size() / 2;
		int[] a = new int[len];
		boolean flag1 = true;
		if (flag1) {
			for (int i = 0; i < v.size() - 2; i = i + 2) {
				if (v.elementAt(i) == 0 && v.elementAt(i + 1) == 1)
					a[i / 2] = 1;
				else if (v.elementAt(i) == 1 && v.elementAt(i + 1) == 0)
					a[i / 2] = 0;
				else {
					if (i < 80)
						flag1 = false;
					break;
				}
			//	System.out.println(i);
			}
		}
		if (!flag1) {
			for (int i = 1; i < v.size() - 2; i = i + 2) {
				if (v.elementAt(i) == 0 && v.elementAt(i + 1) == 1)
					a[(i - 1) / 2] = 1;
				else if (v.elementAt(i) == 1 && v.elementAt(i + 1) == 0)
					a[(i - 1) / 2] = 0;
				else {
					return 0;
				}
			}
		}

		// 解码后，寻找帧头8个0和帧尾8个1，进而得到8位ID
		int head = len;
		for (int i = 0; i < len - 72; i++) {
			if (sum(a, i, 24) == 0 && sum(a, i + 48, 24) == 24) {
				head = i;
				break;
			}
		}
		if (head == len)// 未找到帧头 return 0
			return 0;
		int[] IDArray = new int[24];// IDArray存入的是8位ID
		long id = 0;
		
		for (int i = 0; i < 24; i++) {
			IDArray[i] = a[head + 24 + i];
			id = id << 1;
			id = id | (long)IDArray[i];
			
		}
		
		//	int test = 0;
	//	 test = 128 * IDArray[0] + 64 * IDArray[1] + 32 * IDArray[2] + 16 * IDArray[3] +
	//			 8 * IDArray[4] + 4 * IDArray[5] + 2 * IDArray[6] + IDArray[7];
		
	//	test = (IDArray[0]<<7) | (IDArray[1]<<6) | (IDArray[2]<<5) |(IDArray[3]<<4) |(IDArray[4]<<3) |(IDArray[5]<<2) |(IDArray[6]<<1) |(IDArray[7]) ;
		

		// 二进制转十进制
//		id = 128 * IDArray[0] + 64 * IDArray[1] + 32 * IDArray[2] + 16 * IDArray[3] +
//				 8 * IDArray[4] + 4 * IDArray[5] + 2 * IDArray[6] + IDArray[7];

		return id;
	}

	// 一个求和函数，用来判断帧头帧尾
	private int sum(int[] a, int h, int t) {
		int sum = 0;
	//	System.out.println("size of a = " + a.length + ", need " + (h+t));
		for (int i = h; i < h + t; i++) {
			sum = sum + a[i];
		}
		return sum;
	}
}