/*
 * PROJECT: NyMmd
 * --------------------------------------------------------------------------------
 * The MMD for Java is Java version MMD Motion player class library.
 * NyMmd is modules which removed the ARToolKit origin codes from ARTK_MMD,
 * and was ported to Java.
 *
 * This is based on the ARTK_MMD v0.1 by PY.
 * http://ppyy.if.land.to/artk_mmd.html
 * py1024<at>gmail.com
 * http://www.nicovideo.jp/watch/sm7398691
 *
 *
 * The MIT License
 * Copyright (C)2008-2012 nyatla
 * nyatla39<at>gmail.com
 * http://nyatla.jp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 */
package jp.nyatla.nymmd.types;

public class MmdMatrix {
    //////
    // <NyARToolkitからのポート>
    //////

    /**
     * 行列の要素値です。
     */
    public double m00;
    /**
     * 行列の要素値です。
     */
    public double m01;
    /**
     * 行列の要素値です。
     */
    public double m02;
    /**
     * 行列の要素値です。
     */
    public double m03;
    /**
     * 行列の要素値です。
     */
    public double m10;
    /**
     * 行列の要素値です。
     */
    public double m11;
    /**
     * 行列の要素値です。
     */
    public double m12;
    /**
     * 行列の要素値です。
     */
    public double m13;
    /**
     * 行列の要素値です。
     */
    public double m20;
    /**
     * 行列の要素値です。
     */
    public double m21;
    /**
     * 行列の要素値です。
     */
    public double m22;
    /**
     * 行列の要素値です。
     */
    public double m23;
    /**
     * 行列の要素値です。
     */
    public double m30;
    /**
     * 行列の要素値です。
     */
    public double m31;
    /**
     * 行列の要素値です。
     */
    public double m32;
    /**
     * 行列の要素値です。
     */
    public double m33;

    /**
     * この関数は、オブジェクトの配列を生成して返します。
     *
     * @param i_number 配列の長さ
     * @return 新しいオブジェクト配列
     */
    public static MmdMatrix[] createArray(int i_number) {
        MmdMatrix[] ret = new MmdMatrix[i_number];
        for (int i = 0; i < i_number; i++) {
            ret[i] = new MmdMatrix();
        }
        return ret;
    }

    /**
     * この関数は、要素数16の配列を、行列にセットします。 00,01,02,03,10...の順です。
     */
    public void setValue(double[] i_value) {
        this.m00 = i_value[0];
        this.m01 = i_value[1];
        this.m02 = i_value[2];
        this.m03 = i_value[3];
        this.m10 = i_value[4];
        this.m11 = i_value[5];
        this.m12 = i_value[6];
        this.m13 = i_value[7];
        this.m20 = i_value[8];
        this.m21 = i_value[9];
        this.m22 = i_value[10];
        this.m23 = i_value[11];
        this.m30 = i_value[12];
        this.m31 = i_value[13];
        this.m32 = i_value[14];
        this.m33 = i_value[15];
        return;
    }

    /**
     * この関数は、オブジェクトの内容をインスタンスにコピーします。
     *
     * @param i_value コピー元のオブジェクト
     */
    public void setValue(MmdMatrix i_value) {
        this.m00 = i_value.m00;
        this.m01 = i_value.m01;
        this.m02 = i_value.m02;
        this.m03 = i_value.m03;
        this.m10 = i_value.m10;
        this.m11 = i_value.m11;
        this.m12 = i_value.m12;
        this.m13 = i_value.m13;
        this.m20 = i_value.m20;
        this.m21 = i_value.m21;
        this.m22 = i_value.m22;
        this.m23 = i_value.m23;
        this.m30 = i_value.m30;
        this.m31 = i_value.m31;
        this.m32 = i_value.m32;
        this.m33 = i_value.m33;
        return;
    }

    /**
     * この関数は、要素数16の配列に、行列の内容をコピーします。 順番は、00,01,02,03,10...の順です。
     */
    public void getValue(double[] o_value) {
        o_value[0] = this.m00;
        o_value[1] = this.m01;
        o_value[2] = this.m02;
        o_value[3] = this.m03;
        o_value[4] = this.m10;
        o_value[5] = this.m11;
        o_value[6] = this.m12;
        o_value[7] = this.m13;
        o_value[8] = this.m20;
        o_value[9] = this.m21;
        o_value[10] = this.m22;
        o_value[11] = this.m23;
        o_value[12] = this.m30;
        o_value[13] = this.m31;
        o_value[14] = this.m32;
        o_value[15] = this.m33;
        return;
    }

    public void getValue(float[] o_value) {
        o_value[0] = (float) this.m00;
        o_value[1] = (float) this.m01;
        o_value[2] = (float) this.m02;
        o_value[3] = (float) this.m03;
        o_value[4] = (float) this.m10;
        o_value[5] = (float) this.m11;
        o_value[6] = (float) this.m12;
        o_value[7] = (float) this.m13;
        o_value[8] = (float) this.m20;
        o_value[9] = (float) this.m21;
        o_value[10] = (float) this.m22;
        o_value[11] = (float) this.m23;
        o_value[12] = (float) this.m30;
        o_value[13] = (float) this.m31;
        o_value[14] = (float) this.m32;
        o_value[15] = (float) this.m33;
        return;
    }

    /**
     * この関数は、要素数16の配列に、行列の内容を転置してからコピーします。 順番は、00,10,20,30,01...の順です。
     *
     * @param o_value 値を受け取る配列
     */
    public void getValueT(double[] o_value) {
        o_value[0] = this.m00;
        o_value[1] = this.m10;
        o_value[2] = this.m20;
        o_value[3] = this.m30;
        o_value[4] = this.m01;
        o_value[5] = this.m11;
        o_value[6] = this.m21;
        o_value[7] = this.m31;
        o_value[8] = this.m02;
        o_value[9] = this.m12;
        o_value[10] = this.m22;
        o_value[11] = this.m32;
        o_value[12] = this.m03;
        o_value[13] = this.m13;
        o_value[14] = this.m23;
        o_value[15] = this.m33;
        return;
    }

    /**
     * この関数は、逆行列を計算して、インスタンスにセットします。
     *
     * @param i_src 逆行列を計算するオブジェクト。thisを指定できます。
     * @return 逆行列を得られると、trueを返します。
     */
    public boolean inverse(MmdMatrix i_src) {
        final double a11, a12, a13, a14, a21, a22, a23, a24, a31, a32, a33, a34, a41, a42, a43, a44;
        final double b11, b12, b13, b14, b21, b22, b23, b24, b31, b32, b33, b34, b41, b42, b43, b44;
        double t1, t2, t3, t4, t5, t6;
        a11 = i_src.m00;
        a12 = i_src.m01;
        a13 = i_src.m02;
        a14 = i_src.m03;
        a21 = i_src.m10;
        a22 = i_src.m11;
        a23 = i_src.m12;
        a24 = i_src.m13;
        a31 = i_src.m20;
        a32 = i_src.m21;
        a33 = i_src.m22;
        a34 = i_src.m23;
        a41 = i_src.m30;
        a42 = i_src.m31;
        a43 = i_src.m32;
        a44 = i_src.m33;

        t1 = a33 * a44 - a34 * a43;
        t2 = a34 * a42 - a32 * a44;
        t3 = a32 * a43 - a33 * a42;
        t4 = a34 * a41 - a31 * a44;
        t5 = a31 * a43 - a33 * a41;
        t6 = a31 * a42 - a32 * a41;

        b11 = a22 * t1 + a23 * t2 + a24 * t3;
        b21 = -(a23 * t4 + a24 * t5 + a21 * t1);
        b31 = a24 * t6 - a21 * t2 + a22 * t4;
        b41 = -(a21 * t3 - a22 * t5 + a23 * t6);

        t1 = a43 * a14 - a44 * a13;
        t2 = a44 * a12 - a42 * a14;
        t3 = a42 * a13 - a43 * a12;
        t4 = a44 * a11 - a41 * a14;
        t5 = a41 * a13 - a43 * a11;
        t6 = a41 * a12 - a42 * a11;

        b12 = -(a32 * t1 + a33 * t2 + a34 * t3);
        b22 = a33 * t4 + a34 * t5 + a31 * t1;
        b32 = -(a34 * t6 - a31 * t2 + a32 * t4);
        b42 = a31 * t3 - a32 * t5 + a33 * t6;

        t1 = a13 * a24 - a14 * a23;
        t2 = a14 * a22 - a12 * a24;
        t3 = a12 * a23 - a13 * a22;
        t4 = a14 * a21 - a11 * a24;
        t5 = a11 * a23 - a13 * a21;
        t6 = a11 * a22 - a12 * a21;

        b13 = a42 * t1 + a43 * t2 + a44 * t3;
        b23 = -(a43 * t4 + a44 * t5 + a41 * t1);
        b33 = a44 * t6 - a41 * t2 + a42 * t4;
        b43 = -(a41 * t3 - a42 * t5 + a43 * t6);

        t1 = a23 * a34 - a24 * a33;
        t2 = a24 * a32 - a22 * a34;
        t3 = a22 * a33 - a23 * a32;
        t4 = a24 * a31 - a21 * a34;
        t5 = a21 * a33 - a23 * a31;
        t6 = a21 * a32 - a22 * a31;

        b14 = -(a12 * t1 + a13 * t2 + a14 * t3);
        b24 = a13 * t4 + a14 * t5 + a11 * t1;
        b34 = -(a14 * t6 - a11 * t2 + a12 * t4);
        b44 = a11 * t3 - a12 * t5 + a13 * t6;

        double det_1 = (a11 * b11 + a21 * b12 + a31 * b13 + a41 * b14);
        if (det_1 == 0) {
            return false;
        }
        det_1 = 1 / det_1;

        this.m00 = b11 * det_1;
        this.m01 = b12 * det_1;
        this.m02 = b13 * det_1;
        this.m03 = b14 * det_1;

        this.m10 = b21 * det_1;
        this.m11 = b22 * det_1;
        this.m12 = b23 * det_1;
        this.m13 = b24 * det_1;

        this.m20 = b31 * det_1;
        this.m21 = b32 * det_1;
        this.m22 = b33 * det_1;
        this.m23 = b34 * det_1;

        this.m30 = b41 * det_1;
        this.m31 = b42 * det_1;
        this.m32 = b43 * det_1;
        this.m33 = b44 * det_1;

        return true;
    }

    /**
     * この関数は、行列同士の掛け算をして、インスタンスに格納します。 i_mat_lとi_mat_rには、thisを指定しないでください。
     *
     * @param i_mat_l 左成分の行列
     * @param i_mat_r 右成分の行列
     */
    public final void mul(MmdMatrix i_mat_l, MmdMatrix i_mat_r) {
        assert (this != i_mat_l);
        assert (this != i_mat_r);
        this.m00 = i_mat_l.m00 * i_mat_r.m00 + i_mat_l.m01 * i_mat_r.m10 + i_mat_l.m02 * i_mat_r.m20
                + i_mat_l.m03 * i_mat_r.m30;
        this.m01 = i_mat_l.m00 * i_mat_r.m01 + i_mat_l.m01 * i_mat_r.m11 + i_mat_l.m02 * i_mat_r.m21
                + i_mat_l.m03 * i_mat_r.m31;
        this.m02 = i_mat_l.m00 * i_mat_r.m02 + i_mat_l.m01 * i_mat_r.m12 + i_mat_l.m02 * i_mat_r.m22
                + i_mat_l.m03 * i_mat_r.m32;
        this.m03 = i_mat_l.m00 * i_mat_r.m03 + i_mat_l.m01 * i_mat_r.m13 + i_mat_l.m02 * i_mat_r.m23
                + i_mat_l.m03 * i_mat_r.m33;

        this.m10 = i_mat_l.m10 * i_mat_r.m00 + i_mat_l.m11 * i_mat_r.m10 + i_mat_l.m12 * i_mat_r.m20
                + i_mat_l.m13 * i_mat_r.m30;
        this.m11 = i_mat_l.m10 * i_mat_r.m01 + i_mat_l.m11 * i_mat_r.m11 + i_mat_l.m12 * i_mat_r.m21
                + i_mat_l.m13 * i_mat_r.m31;
        this.m12 = i_mat_l.m10 * i_mat_r.m02 + i_mat_l.m11 * i_mat_r.m12 + i_mat_l.m12 * i_mat_r.m22
                + i_mat_l.m13 * i_mat_r.m32;
        this.m13 = i_mat_l.m10 * i_mat_r.m03 + i_mat_l.m11 * i_mat_r.m13 + i_mat_l.m12 * i_mat_r.m23
                + i_mat_l.m13 * i_mat_r.m33;

        this.m20 = i_mat_l.m20 * i_mat_r.m00 + i_mat_l.m21 * i_mat_r.m10 + i_mat_l.m22 * i_mat_r.m20
                + i_mat_l.m23 * i_mat_r.m30;
        this.m21 = i_mat_l.m20 * i_mat_r.m01 + i_mat_l.m21 * i_mat_r.m11 + i_mat_l.m22 * i_mat_r.m21
                + i_mat_l.m23 * i_mat_r.m31;
        this.m22 = i_mat_l.m20 * i_mat_r.m02 + i_mat_l.m21 * i_mat_r.m12 + i_mat_l.m22 * i_mat_r.m22
                + i_mat_l.m23 * i_mat_r.m32;
        this.m23 = i_mat_l.m20 * i_mat_r.m03 + i_mat_l.m21 * i_mat_r.m13 + i_mat_l.m22 * i_mat_r.m23
                + i_mat_l.m23 * i_mat_r.m33;

        this.m30 = i_mat_l.m30 * i_mat_r.m00 + i_mat_l.m31 * i_mat_r.m10 + i_mat_l.m32 * i_mat_r.m20
                + i_mat_l.m33 * i_mat_r.m30;
        this.m31 = i_mat_l.m30 * i_mat_r.m01 + i_mat_l.m31 * i_mat_r.m11 + i_mat_l.m32 * i_mat_r.m21
                + i_mat_l.m33 * i_mat_r.m31;
        this.m32 = i_mat_l.m30 * i_mat_r.m02 + i_mat_l.m31 * i_mat_r.m12 + i_mat_l.m32 * i_mat_r.m22
                + i_mat_l.m33 * i_mat_r.m32;
        this.m33 = i_mat_l.m30 * i_mat_r.m03 + i_mat_l.m31 * i_mat_r.m13 + i_mat_l.m32 * i_mat_r.m23
                + i_mat_l.m33 * i_mat_r.m33;
        return;
    }

    /**
     * この関数は、行列を単位行列にします。
     */
    public final void identity() {
        this.m00 = this.m11 = this.m22 = this.m33 = 1;
        this.m01 = this.m02 = this.m03 = this.m10 = this.m12 = this.m13 = this.m20 = this.m21 = this.m23 = this.m30 = this.m31 = this.m32 = 0;
        return;
    }

    public void MatrixLerp(MmdMatrix sm1, MmdMatrix sm2, float fLerpValue) {
        double fT = 1.0 - fLerpValue;
        this.m00 = sm1.m00 * fLerpValue + sm2.m00 * fT;
        this.m01 = sm1.m01 * fLerpValue + sm2.m01 * fT;
        this.m02 = sm1.m02 * fLerpValue + sm2.m02 * fT;
        this.m03 = sm1.m03 * fLerpValue + sm2.m03 * fT;
        this.m10 = sm1.m10 * fLerpValue + sm2.m10 * fT;
        this.m11 = sm1.m11 * fLerpValue + sm2.m11 * fT;
        this.m12 = sm1.m12 * fLerpValue + sm2.m12 * fT;
        this.m13 = sm1.m13 * fLerpValue + sm2.m13 * fT;
        this.m20 = sm1.m20 * fLerpValue + sm2.m20 * fT;
        this.m21 = sm1.m21 * fLerpValue + sm2.m21 * fT;
        this.m22 = sm1.m22 * fLerpValue + sm2.m22 * fT;
        this.m23 = sm1.m23 * fLerpValue + sm2.m23 * fT;
        this.m30 = sm1.m30 * fLerpValue + sm2.m30 * fT;
        this.m31 = sm1.m31 * fLerpValue + sm2.m31 * fT;
        this.m32 = sm1.m32 * fLerpValue + sm2.m32 * fT;
        this.m33 = sm1.m33 * fLerpValue + sm2.m33 * fT;
        return;
    }

    public void QuaternionToMatrix(MmdVector4 pvec4Quat) {
        double x2 = pvec4Quat.x * pvec4Quat.x * 2.0f;
        double y2 = pvec4Quat.y * pvec4Quat.y * 2.0f;
        double z2 = pvec4Quat.z * pvec4Quat.z * 2.0f;
        double xy = pvec4Quat.x * pvec4Quat.y * 2.0f;
        double yz = pvec4Quat.y * pvec4Quat.z * 2.0f;
        double zx = pvec4Quat.z * pvec4Quat.x * 2.0f;
        double xw = pvec4Quat.x * pvec4Quat.w * 2.0f;
        double yw = pvec4Quat.y * pvec4Quat.w * 2.0f;
        double zw = pvec4Quat.z * pvec4Quat.w * 2.0f;

        this.m00 = 1.0f - y2 - z2;
        this.m01 = xy + zw;
        this.m02 = zx - yw;
        this.m10 = xy - zw;
        this.m11 = 1.0f - z2 - x2;
        this.m12 = yz + xw;
        this.m20 = zx + yw;
        this.m21 = yz - xw;
        this.m22 = 1.0f - x2 - y2;

        this.m03 = this.m13 = this.m23 = this.m30 = this.m31 = this.m32 = 0.0f;
        this.m33 = 1.0f;
        return;
    }

    public MmdVector3 getPos() {
        return new MmdVector3((float) m30, (float) m31, (float) m32);
    }

    public MmdVector3 getRotXYZ() {
        // abc xyz
        double a, b, c;

        b = (float) Math.asin(-m02);
        double bb = Math.cos(b);

        if (Double.isNaN(bb) || Math.abs(bb) <= 0.0001) {
            c = Math.atan2(-m10, m11);
            // b = Math.asin(-m02);
            a = 0;
        } else {
            c = (float) Math.atan2(m01, m00);
            // b = (float)Math.asin(-m02);
            a = (float) Math.asin(m12 / bb);
            if (m22 < 0) {
                a = Math.PI - a;
            }
        }

        if (Double.isNaN(a) || Double.isInfinite(a)) {
            a = 0;
        }
        if (Double.isNaN(b) || Double.isInfinite(b)) {
            b = 0;
        }
        if (Double.isNaN(c) || Double.isInfinite(c)) {
            c = 0;
        }

        return new MmdVector3((float) a, (float) b, (float) c);
    }

    /*
     * private static final DoubleBuffer matrixBuffer =
     * MemoryTracker.createByteBuffer(16 << 4).asDoubleBuffer(); public DoubleBuffer
     * getMatrixBuffer(){ double buf[] = new double[16]; this.getValue(buf);
     * matrixBuffer.position(0); matrixBuffer.put(buf); matrixBuffer.position(0);
     * return matrixBuffer; }
     */
}
