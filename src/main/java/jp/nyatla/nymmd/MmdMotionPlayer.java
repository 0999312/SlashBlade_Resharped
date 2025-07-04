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
package jp.nyatla.nymmd;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import jp.nyatla.nymmd.core.PmdBone;
import jp.nyatla.nymmd.core.PmdFace;
import jp.nyatla.nymmd.core.PmdIK;
import jp.nyatla.nymmd.types.FaceData;
import jp.nyatla.nymmd.types.MmdMatrix;
import jp.nyatla.nymmd.types.MmdVector3;
import jp.nyatla.nymmd.types.MotionData;

import java.util.Map;
import java.util.stream.IntStream;

public abstract class MmdMotionPlayer {
    protected MmdPmdModel_BasicClass _ref_pmd_model;
    protected MmdVmdMotion_BasicClass _ref_vmd_motion;

    private PmdBone[] m_ppBoneList;
    private PmdFace[] m_ppFaceList;

    public MmdMatrix[] _skinning_mat;
    public Map<String, Integer> boneNameToIndex = Maps.newHashMap();

    public int getBoneIndexByName(String name) {
        Integer result = boneNameToIndex.get(name);
        return result == null ? -1 : result;
        /*
         * for(int i = 0; i < this._ref_pmd_model.getBoneArray().length; i++){
         * if(this._ref_pmd_model.getBoneArray()[i].getName().equals(name)){ return i; }
         * } return -1;
         */
    }

    public PmdBone getBoneByName(String name) {
        int idx = getBoneIndexByName(name);
        if (idx < 0) {
            return null;
        } else {
            return this._ref_pmd_model.getBoneArray()[idx];
        }

        /*
         * for(PmdBone bone : this._ref_pmd_model.getBoneArray()){
         * if(bone.getName().equals(name)){ return bone; } } return null;
         */
    }

    private PmdBone m_pNeckBone; // 首のボーン

    public MmdMotionPlayer() {
        return;
    }

    public void setPmd(MmdPmdModel_BasicClass i_pmd_model) throws MmdException {
        this._ref_pmd_model = i_pmd_model;
        PmdBone[] bone_array = i_pmd_model.getBoneArray();
        // スキニング用のmatrix
        this._skinning_mat = MmdMatrix.createArray(bone_array.length);

        boneNameToIndex.clear();
        IntStream.range(0, bone_array.length).forEach(value -> boneNameToIndex.put(bone_array[value].getName(), value));

        // 首^H頭のボーンを探しておく
        this.m_pNeckBone = null;
        Integer headIdx = boneNameToIndex.get("頭");
        if (headIdx != null) {
            this.m_pNeckBone = bone_array[headIdx];
        }
        /*
         * for(int i=0;i<bone_array.length;i++){
         * if(bone_array[i].getName().equals("頭")){ this.m_pNeckBone = bone_array[i];
         * break; } }
         */

        // PMD/VMDが揃った？
        if (this._ref_vmd_motion != null) {
            makeBoneFaceList();
        }
        return;
    }

    public void setVmd(MmdVmdMotion_BasicClass i_vmd_model) throws MmdException {
        if (this._ref_vmd_motion == i_vmd_model) {
            // It already set the same.
            return;
        }

        this._ref_vmd_motion = i_vmd_model;
        // 操作対象ボーンのポインタを設定する
        MotionData[] pMotionDataList = i_vmd_model.refMotionDataArray();
        this.m_ppBoneList = new PmdBone[pMotionDataList.length];
        // 操作対象表情のポインタを設定する
        FaceData[] pFaceDataList = i_vmd_model.refFaceDataArray();
        this.m_ppFaceList = new PmdFace[pFaceDataList.length];
        // PMD/VMDが揃った？
        if (this._ref_pmd_model != null) {
            makeBoneFaceList();
        }
        return;
    }

    private void makeBoneFaceList() {
        MmdPmdModel_BasicClass pmd_model = this._ref_pmd_model;
        MmdVmdMotion_BasicClass vmd_model = this._ref_vmd_motion;

        // 操作対象ボーンのポインタを設定する
        MotionData[] pMotionDataList = vmd_model.refMotionDataArray();
        this.m_ppBoneList = new PmdBone[pMotionDataList.length];
        for (int i = 0; i < pMotionDataList.length; i++) {
            this.m_ppBoneList[i] = pmd_model.getBoneByName(pMotionDataList[i].szBoneName);
        }
        // 操作対象表情のポインタを設定する
        FaceData[] pFaceDataList = vmd_model.refFaceDataArray();
        this.m_ppFaceList = new PmdFace[pFaceDataList.length];
        for (int i = 0; i < pFaceDataList.length; i++) {
            this.m_ppFaceList[i] = pmd_model.getFaceByName(pFaceDataList[i].szFaceName);
        }
        return;
    }

    /**
     * VMDの再生時間長を返します。
     *
     * @return ms単位の再生時間
     */
    public float getTimeLength() {
        return (float) (this._ref_vmd_motion.getMaxFrame() * (100.0 / 3));
    }

    /**
     * 指定した時刻のモーションに更新します。
     *
     * @param i_position_in_msec モーションの先頭からの時刻をms単位で指定します。
     * @throws MmdException
     */
    public void updateMotion(float i_position_in_msec) throws MmdException {
        final PmdIK[] ik_array = this._ref_pmd_model.getIKArray();
        final PmdBone[] bone_array = this._ref_pmd_model.getBoneArray();
        assert i_position_in_msec >= 0;
        // 描画するフレームを計算する。
        float frame = (float) (i_position_in_msec / (100.0 / 3));
        // 範囲外を除外
        if (frame > this._ref_vmd_motion.getMaxFrame()) {
            frame = this._ref_vmd_motion.getMaxFrame();
        }
        this.updateFace(frame);

        // 累積IK反映値初期化
        for (PmdBone bone : bone_array) {
            bone.reset();
        }

        // モーション更新
        this.updateBone(frame);

        eventBus.post(new UpdateBoneEvent.Pre(this._ref_pmd_model.getBoneArray(), this));

        // ボーン行列の更新
        for (int i = 0; i < bone_array.length; i++) {
            bone_array[i].updateMatrix();
        }

        // IKの更新
        for (int i = 0; i < ik_array.length; i++) {
            ik_array[i].update();
        }

        eventBus.post(new UpdateBoneEvent.Pre(this._ref_pmd_model.getBoneArray(), this));
        // ボーン行列の更新
        for (int i = 0; i < bone_array.length; i++) {
            bone_array[i].updateMatrix();
        }

        // Lookme!
        if (this._lookme_enabled) {
            this.updateNeckBone();
        }
        //
        // スキニング用行列の更新
        for (int i = 0; i < bone_array.length; i++) {
            bone_array[i].updateSkinningMat(this._skinning_mat[i]);
        }
        this.onUpdateSkinningMatrix(this._skinning_mat);
        return;
    }

    protected abstract void onUpdateSkinningMatrix(MmdMatrix[] i_skinning_mat) throws MmdException;

    // programmable bone control -----------

    public final EventBus eventBus = new EventBus();

    static public class UpdateBoneEvent {
        public final PmdBone[] bones;
        public final MmdMotionPlayer motionPlayer;

        public UpdateBoneEvent(PmdBone[] bones, MmdMotionPlayer motionPlayer) {
            this.bones = bones;
            this.motionPlayer = motionPlayer;
        }

        static public class Pre extends UpdateBoneEvent {
            public Pre(PmdBone[] bones, MmdMotionPlayer motionPlayer) {
                super(bones, motionPlayer);
            }
        }

        static public class Post extends UpdateBoneEvent {
            public Post(PmdBone[] bones, MmdMotionPlayer motionPlayer) {
                super(bones, motionPlayer);
            }
        }
    }

    // pbc end -----------

    public void setLookVector(float i_x, float i_y, float i_z) {
        this._looktarget.x = i_x;
        this._looktarget.y = i_y;
        this._looktarget.z = i_z;
    }

    public void lookMeEnable(boolean i_enable) {
        this._lookme_enabled = i_enable;
    }

    private MmdVector3 _looktarget = new MmdVector3();
    private boolean _lookme_enabled = false;

    /**
     * look me
     *
     * @param pvec3LookTarget
     */
    private void updateNeckBone() {
        if (this.m_pNeckBone == null) {
            return;
        }
        this.m_pNeckBone.lookAt(this._looktarget);

        PmdBone[] bone_array = this._ref_pmd_model.getBoneArray();
        int i;
        for (i = 0; i < bone_array.length; i++) {
            if (this.m_pNeckBone == bone_array[i]) {
                break;
            }
        }
        for (; i < bone_array.length; i++) {
            bone_array[i].updateMatrix();
        }
        return;
    }

    private void updateBone(float i_frame) throws MmdException {
        // ---------------------------------------------------------
        // 指定フレームのデータでボーンを動かす
        final PmdBone[] ppBone = this.m_ppBoneList;

        MotionData[] pMotionDataList = _ref_vmd_motion.refMotionDataArray();
        for (int i = 0; i < pMotionDataList.length; i++) {
            if (ppBone[i] == null) {
                continue;
            }
            pMotionDataList[i].getMotionPosRot(i_frame, ppBone[i]);
//			ppBone[i].m_vec3Position.setValue(vec3Position);
            // 補間あり
            // Vector3Lerp( &((*pBone)->m_vec3Position), &((*pBone)->m_vec3Position),
            // &vec3Position, fLerpValue );
            // QuaternionSlerp( &((*pBone)->m_vec4Rotate), &((*pBone)->m_vec4Rotate),
            // &vec4Rotate, fLerpValue );
        }
        return;
    }

    /**
     * 指定フレームのデータで表情を変形する
     *
     * @param i_frame
     * @throws MmdException
     */
    private void updateFace(float i_frame) throws MmdException {
        final MmdVector3[] position_array = this._ref_pmd_model.getPositionArray();
        PmdFace[] ppFace = this.m_ppFaceList;
        FaceData[] pFaceDataList = _ref_vmd_motion.refFaceDataArray();
        for (int i = 0; i < pFaceDataList.length; i++) {
            final float fFaceRate = getFaceRate(pFaceDataList[i], i_frame);
            if (ppFace[i] == null) {
                continue;
            }
            if (fFaceRate == 1.0f) {
                ppFace[i].setFace(position_array);
            } else if (0.001f < fFaceRate) {
                ppFace[i].blendFace(position_array, fFaceRate);
            }
        }
        return;
    }

    private float getFaceRate(FaceData pFaceData, float fFrame) {
        int i;
        int ulNumKeyFrame = pFaceData.ulNumKeyFrames;

        // 最終フレームを過ぎていた場合
        if (fFrame > pFaceData.pKeyFrames[ulNumKeyFrame - 1].fFrameNo) {
            fFrame = pFaceData.pKeyFrames[ulNumKeyFrame - 1].fFrameNo;
        }

        // 現在の時間がどのキー近辺にあるか
        for (i = 0; i < ulNumKeyFrame; i++) {
            if (fFrame <= pFaceData.pKeyFrames[i].fFrameNo) {
                break;
            }
        }

        // 前後のキーを設定
        int lKey0 = i - 1;
        int lKey1 = i;

        if (lKey0 <= 0) {
            lKey0 = 0;
        }
        if (i == ulNumKeyFrame) {
            lKey1 = ulNumKeyFrame - 1;
        }

        // 前後のキーの時間
        float fTime0 = pFaceData.pKeyFrames[lKey0].fFrameNo;
        float fTime1 = pFaceData.pKeyFrames[lKey1].fFrameNo;

        // 前後のキーの間でどの位置にいるか
        float fLerpValue;
        if (lKey0 != lKey1) {
            fLerpValue = (fFrame - fTime0) / (fTime1 - fTime0);
            return (pFaceData.pKeyFrames[lKey0].fRate * (1.0f - fLerpValue))
                    + (pFaceData.pKeyFrames[lKey1].fRate * fLerpValue);
        } else {
            return pFaceData.pKeyFrames[lKey0].fRate;
        }
    }

}
