package com.jifenqi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementArray;
import org.simpleframework.xml.ElementList;

public class GameInfo {

    @Element(name="Type")
    public String mType;
    @Element(name="Version")
    public String mVersion;
    @Element(name="StartTime")
    public String mStartTime;
    @Element(name="StartZhuangjiaId")
    public int mStartZhuangjiaId;
    
    @Element(name="PlayerNumber")
    public int mPlayerNumber;
    @Element(name="HasShangXiaXing")
    public boolean mHasShangxiaxing;
    @ElementArray(name="PlayerName", entry="name")
    public String[] mPlayerNames;
    @ElementList(name="Rounds", inline=true, entry="Round")
    public ArrayList<RoundInfo> mRoundInfos;
    public ArrayList<int[]> mPointsCache;
    @ElementArray(name="StartPoints")
    public int[] mStartPoints;
    
    public GameInfo() {
        mType = Const.ZIPAI;
        mVersion = Const.VERSION;
        
        mRoundInfos = new ArrayList<RoundInfo>();
        mPointsCache = new ArrayList<int[]>();
        
        setStartTime();
    }
    
    private void setStartTime() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(Const.DATE_FORMAT);
        mStartTime = sdf.format(now);
    }
    
    public int[] getRemainPoints() {
        int index = mPointsCache.size() - 1;
        if(index < 0)
            return new int[mPlayerNumber];
        
        int[] lastRoundPoints = mPointsCache.get(index);
        int[] remainPoints = caculateRemainPoints(lastRoundPoints);
        return remainPoints;
    }
    
    private int[] caculateRemainPoints(int[] points) {
        int[] result = new int[points.length];
        System.arraycopy(points, 0, result, 0, points.length);
       
        boolean comsumed = true;
        for(int i = 0; i < result.length; i++) {
            while(result[i] >= 10 && comsumed) {
                comsumed = false;
                result[i] -= 10;
                for(int j = 0; j < result.length; j++) {
                    if(j != i && result[j] <= -10) {
                        result[j] += 10;
                        comsumed = true;
                        break;
                    }
                }
                if(!comsumed) {
                    result[i] += 10;
                }
            }
        }
       
//        String str = new String("result: ");
//        for(int i = 0; i < result.length; i++) {
//            str += result[i];
//            str += ", ";
//        }
//        Log.d(TAG, str);
        return result;
    }
    
    public void addRoundResult(RoundInfo ri) {
        mRoundInfos.add(ri);
        int round = mPointsCache.size();
        int[] lastRoundPoints;
        if(round != 0) {
            lastRoundPoints = mPointsCache.get(round - 1);
        } else {
            lastRoundPoints = mStartPoints;
        }
        int[] newRoundPoints = calcPoints(lastRoundPoints, ri);
        mPointsCache.add(newRoundPoints);
    }
    
    public void addRoundResult(int zhuangjia, int hupaiPlayerId, int huzi, int shangxing, int xiaxing, boolean isZimo, int fangpaoPlayer) {
        RoundInfo ri = new RoundInfo();
        
        ri.zhuangjiaId = zhuangjia;
        ri.hupaiPlayerId = hupaiPlayerId;
        ri.huzishu = huzi;
        ri.shangxing = shangxing;
        ri.xiaxing = xiaxing;
        ri.zimo = isZimo;
        ri.fangpaoPlayerId = fangpaoPlayer;
        
        addRoundResult(ri);
    }
    
    public void updateRound(int roundId, RoundInfo ri) {
        mRoundInfos.set(roundId, ri);
        refreshPointsCache(roundId);
    }
    
    public void deleteRound(int position) {
        mRoundInfos.remove(position);
        refreshPointsCache(position);
    }
    
    private int[] calcPoints(int[] preRoundPoints, RoundInfo ri) {
        int[] newRoundPoints = new int[preRoundPoints.length];
        
        //huang zhuang
        if(ri.hupaiPlayerId == -1) {
            int perPoint = 3;
            for(int i = 0; i < newRoundPoints.length; i++) {
                if(i == ri.zhuangjiaId) {
                    int lostPoint = perPoint * (mPlayerNumber - 1);
                    newRoundPoints[i] = preRoundPoints[i] - lostPoint;
                } else {
                    newRoundPoints[i] = preRoundPoints[i] + perPoint;
                }
            }
            return newRoundPoints;
        }
        
        //cha hu zi
        if(ri.huzishu < 0) {
            int perPoint = Math.abs(ri.huzishu) / 5;
            for(int i = 0; i < newRoundPoints.length; i++) {
                if(i == ri.hupaiPlayerId) {
                    int lostPoint = perPoint * (mPlayerNumber - 1);
                    newRoundPoints[i] = preRoundPoints[i] - lostPoint;
                } else {
                    newRoundPoints[i] = preRoundPoints[i] + perPoint;
                }
            }
            return newRoundPoints;
        }
        
        int zhuangjiaPoint = ri.huzishu / 5;
        int shuxingPlayer = Utils.getShuxingPlayer(ri.zhuangjiaId);
        if(mPlayerNumber != 4) {
            shuxingPlayer = -1; //No shu xing.
        }
        boolean hasFangpao = ri.fangpaoPlayerId != -1;
        for(int i = 0; i < newRoundPoints.length; i++) {
            if(i == ri.hupaiPlayerId) {
                int getPoint = (zhuangjiaPoint + ri.shangxing) * 2;
                if(ri.zimo || ri.fangpaoPlayerId != -1) {
                    getPoint *=2;
                }
                newRoundPoints[i] = preRoundPoints[i] + getPoint;
            } else if(i == shuxingPlayer) {
                int getPoint = ri.xiaxing * 2;
                if(ri.zimo || ri.fangpaoPlayerId != -1) {
                    getPoint *= 2;
                }
                newRoundPoints[i] = preRoundPoints[i] + getPoint;
            } else if(i == ri.fangpaoPlayerId) {
                int lostPoint = (zhuangjiaPoint + ri.shangxing + ri.xiaxing) * 4;
                newRoundPoints[i] = preRoundPoints[i] - lostPoint;
            } else {
                if(!hasFangpao) {
                    int lostPoint = zhuangjiaPoint + ri.shangxing + ri.xiaxing;
                    if(ri.zimo) {
                        lostPoint *=2;
                    }
                    newRoundPoints[i] = preRoundPoints[i] - lostPoint;
                } else {
                    newRoundPoints[i] = preRoundPoints[i];
                }
            }
        }
        return newRoundPoints;
    }
    
    public void initPoints(int[] points) {
        RoundInfo ri = new RoundInfo();
        ri.zhuangjiaId = mStartZhuangjiaId;
        //Add dummy round as first round.
        mRoundInfos.add(ri);
        mPointsCache.add(points);
    }
    
    public void refreshPointsCache(int startIndex) {
        ArrayList<int[]> newPoints = new ArrayList<int[]>();
        for(int i = 0; i < mRoundInfos.size(); i ++) {
            if(i < startIndex) {
                newPoints.add(mPointsCache.get(i));
            } else {
                int[] prePoints;
                if(i == 0) {
                    newPoints.add(mStartPoints);
                } else {
                    prePoints = newPoints.get(i - 1);
                    RoundInfo ri = mRoundInfos.get(i);
                    int[] points = calcPoints(prePoints, ri);
                    newPoints.add(points);
                }
            }
        }
        mPointsCache = newPoints;
    }
    
    //The previous hupai player is the current zhuangjia.
    public int getLastZhuangjiaId() {
        RoundInfo ri;
        int zhuangjiaId;
        int count = mRoundInfos.size();
        if(count == 0) {
            zhuangjiaId = mStartZhuangjiaId;
        } else {
            ri = mRoundInfos.get(count - 1);
            zhuangjiaId = ri.hupaiPlayerId;
            if(zhuangjiaId == -1) {
                zhuangjiaId = ri.zhuangjiaId;
            }
        }
        return zhuangjiaId;
    }
    
    public static class RoundInfo {
        @Attribute
        public int zhuangjiaId;
        @Attribute
        public int hupaiPlayerId;
        @Attribute
        public int huzishu;
        @Attribute
        public int shangxing;
        @Attribute
        public int xiaxing;
        @Attribute
        public boolean zimo;
        @Attribute
        public int fangpaoPlayerId;
        
        public RoundInfo() {
            zhuangjiaId = -1;
            hupaiPlayerId = -1;
            fangpaoPlayerId = -1;
        }
    }
}
