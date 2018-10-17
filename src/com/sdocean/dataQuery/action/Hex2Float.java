package com.sdocean.dataQuery.action;

public class Hex2Float {
	public static void main(String[] args) {
			//8.16848
		
		String hex="41cccccd";

        Float  value=Float.intBitsToFloat(Integer.valueOf(hex, 16));
		System.out.println(value);
	}
	

public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }
private static byte toByte(char c) {
    byte b = (byte) "0123456789abcdef".indexOf(c);
    return b;
}


public static float bytesToFloat(byte[] data) {// 解析4个字节中的数据，按照IEEE754的标准
	int s = 0;// 浮点数的符号
	float f = 0;// 浮点数
	int e = 0;// 指数
	if ((data[3] & 0xff) >= 128) {// 求s
		s = -1;
	} else {
		s = 1;
	}
	int temp = 0;// 指数位的最后一位
	if ((data[2] & 0xff) >= 128) {
		temp = 1;
	} else
		temp = 0;
	e = ((data[3] & 0xff) % 128) * 2 + temp;// 求e
	// f=((data[2]&0xff)-temp*128+128)/128+(data[1]&0xff)/(128*256)+(data[0]&0xff)/(128*256*256);
	float[] data2 = new float[3];
	data2[0] = data[0] & 0xff;
	data2[1] = data[1] & 0xff;
	data2[2] = data[2] & 0xff;
	f = (data2[2] - temp * 128 + 128) / 128 + data2[1] / (128 * 256)
			+ data2[0] / (128 * 256 * 256);
	float result = 0;
	if (e == 0 && f != 0) {// 次正规数
		result = (float) (s * (f - 1) * Math.pow(2, -126));
		return result;
	}
	if (e == 0 && f == 0) {// 有符号的0
		result = (float) 0.0;
		return result;
	}
	if (s == 0 && e == 255 && f == 0) {// 正无穷大
		result = (float) 1111.11;
		return result;
	}
	if (s == 1 && e == 255 && f == 0) {// 负无穷大
		result = (float) -1111.11;
		return result;
	} else {
		result = (float) (s * f * Math.pow(2, e - 127));
		return result;
	}

}

public static byte[] floatToBytes(float a) {
	byte[] data = new byte[4];
	if(a==0){
		for(int i=0;i<4;i++){
			data[i]=0x00;
		}
		return data;
	}
	Integer[] intdata = { 0, 0, 0, 0 };
	a = Math.abs(a);
	// 首先将浮点数转化为二进制浮点数
	float floatpart = a % 1;
	int intpart = (int) (a / 1);

	System.out.println(intpart + " " + floatpart);
	// 将整数部分化为2进制,并转化为string类型
	String intString = "";
	String floatString = "";
	String result = "";
	String subResult = "";
	int zhishu = 0;
	if (intpart == 0) {
		intString += "0";
	}
	while (intpart != 0) {
		intString = intpart % 2 + intString;
		intpart = intpart / 2;
	}
	while (floatpart != 0) {
		floatpart *= 2;
		if (floatpart >= 1) {
			floatString += "1";
			floatpart -= 1;
		} else {
			floatString += "0";
		}

	}

	result = intString + floatString;
	System.out.println(intString + "." + floatString);
	intpart = (int) (a / 1);
	if (intpart > 0) {// 整数部分肯定有1，且以1开头..这样的话，小数点左移
		zhishu = intString.length() - 1;
	} else {// 整数位为0，右移
		for (int i = 0; i < floatString.length(); i++) {
			zhishu--;
			if (floatString.charAt(i) == '1') {
				break;
			}
		}
		// while(floatString.charAt(index)){}
	}
	// 对指数进行移码操作

	System.out.println("result==" + result + " zhishu==" + zhishu);
	if (zhishu >= 0) {
		subResult = result.substring(intString.length() - zhishu);
	} else {
		subResult = floatString.substring(-zhishu);
	}
	System.out.println("subResult==" + subResult);
	zhishu += 127;
	if (subResult.length() <= 7) {// 若长度

		for (int i = 0; i < 7; i++) {
			if (i < subResult.length()) {
				intdata[1] = intdata[1] * 2 + subResult.charAt(i) - '0';
			} else {
				intdata[1] *= 2;
			}

		}

		if (zhishu % 2 == 1) {// 如果质数是奇数，则需要在这个最前面加上一个‘1’
			intdata[1] += 128;
		}
		data[1] = intdata[1].byteValue();
	} else if (subResult.length() <= 15) {// 长度在（7,15）以内
		int i = 0;
		for (i = 0; i < 7; i++) {// 计算0-7位，最后加上第一位
			intdata[1] = intdata[1] * 2 + subResult.charAt(i) - '0';
		}
		if (zhishu % 2 == 1) {// 如果质数是奇数，则需要在这个最前面加上一个‘1’
			intdata[1] += 128;
		}
		data[1] = intdata[1].byteValue();

		for (i = 7; i < 15; i++) {// 计算8-15位
			if (i < subResult.length()) {
				intdata[2] = intdata[2] * 2 + subResult.charAt(i) - '0';
			} else {
				intdata[2] *= 2;
			}

		}
		data[2] = intdata[2].byteValue();
	} else {// 长度大于15
		int i = 0;
		for (i = 0; i < 7; i++) {// 计算0-7位，最后加上第一位
			intdata[1] = intdata[1] * 2 + subResult.charAt(i) - '0';
		}
		if (zhishu % 2 == 1) {// 如果质数是奇数，则需要在这个最前面加上一个‘1’
			intdata[1] += 128;
		}
		data[1] = intdata[1].byteValue();

		for (i = 7; i < 15; i++) {// 计算8-15位
			intdata[2] = intdata[2] * 2 + subResult.charAt(i) - '0';
		}
		data[2] = intdata[2].byteValue();

		for (i = 15; i < 23; i++) {// 计算8-15位
			if (i < subResult.length()) {
				intdata[3] = intdata[3] * 2 + subResult.charAt(i) - '0';
			} else {
				intdata[3] *= 2;
			}

		}
		data[3] = intdata[3].byteValue();
	}

	intdata[0] = zhishu / 2;
	if (a < 0) {
		intdata[0] += 128;
	}
	data[0] = intdata[0].byteValue();
	byte[] data2 = new byte[4];// 将数据转移，目的是倒换顺序
	for (int i = 0; i < 4; i++) {
		data2[i] = data[3 - i];
	}
	return data2;
}

 
	/**
	 * 代码来自：java.lang.Long
	 * 因为要跟踪看变量的值，所以要copy出来，或者是去附加源码，否则 eclipse 调试时查看变量的值会提示 xxx cannot be resolved to a variable
	 * @author 微wx笑
	 * @date   2017年12月6日下午5:19:40
	 * @param s
	 * @param radix
	 * @return
	 * @throws NumberFormatException
	 */
	public static long parseLong(String s, int radix) throws NumberFormatException {
		if (s == null) {
			throw new NumberFormatException("null");
		}
 
		if (radix < Character.MIN_RADIX) {
			throw new NumberFormatException("radix " + radix + " less than Character.MIN_RADIX");
		}
		if (radix > Character.MAX_RADIX) {
			throw new NumberFormatException("radix " + radix + " greater than Character.MAX_RADIX");
		}
 
		long result = 0;
		boolean negative = false;
		int i = 0, len = s.length();
		long limit = -Long.MAX_VALUE;
		long multmin;
		int digit;
 
		if (len > 0) {
			char firstChar = s.charAt(0);
			if (firstChar < '0') { // Possible leading "+" or "-"
				if (firstChar == '-') {
					negative = true;
					limit = Long.MIN_VALUE;
				} else if (firstChar != '+')
					throw NumberFormatException.forInputString(s);
 
				if (len == 1) // Cannot have lone "+" or "-"
					throw NumberFormatException.forInputString(s);
				i++;
			}
			multmin = limit / radix;
			while (i < len) {
				// Accumulating negatively avoids surprises near MAX_VALUE
				digit = Character.digit(s.charAt(i++), radix);
				if (digit < 0) {
					throw NumberFormatException.forInputString(s);
				}
				if (result < multmin) {
					throw NumberFormatException.forInputString(s);
				}
				result *= radix;
				if (result < limit + digit) {
					throw NumberFormatException.forInputString(s);
				}
				result -= digit;
			}
		} else {
			throw NumberFormatException.forInputString(s);
		}
		return negative ? result : -result;
	}
}
 
/**
 * 代码来自：java.lang.NumberFormatException
 * NumberFormatException
 * @author 微wx笑
 * @date   2017年12月6日下午5:20:36
 */
class NumberFormatException extends IllegalArgumentException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
 
	public NumberFormatException(String s) {
		super(s);
	}
 
	static NumberFormatException forInputString(String s) {
		return new NumberFormatException("For input string: \"" + s + "\"");
	}
}
