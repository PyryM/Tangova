package com.mtknn.tangova;

import android.util.Base64;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TangoDepthGridulator {
	protected byte[] mGridImage;
	protected float[] mGridAvg;
	protected int[] mGridCounts;
	protected int mGridRows;
	protected int mGridCols;
	protected float mGridWidth;
	protected float mGridHeight;
	protected float mMinDepth;
	protected float mMaxDepth;
	protected boolean mClampDepth;

	public TangoDepthGridulator(int gridCols, int gridRows) {
		mGridCols = gridCols;
		mGridRows = gridRows;

		mGridImage = new byte[gridCols * gridRows];
		mGridAvg = new float[gridCols * gridRows];
		mGridCounts = new int[gridCols * gridRows];

		mMinDepth = 0.1f;
		mMaxDepth = 2.0f;
		mGridWidth = 1.0f; // 90 degree fov by default
		mGridHeight = 1.0f;
		mClampDepth = true;
	}

	public void setGridParams(float gridWidth, float gridHeight, 
							  float minDepth, float maxDepth, 
							  boolean clampDepth) {
		mGridWidth = gridWidth;
		mGridHeight = gridHeight;
		mMaxDepth = maxDepth;
		mMinDepth = minDepth;
		mClampDepth = clampDepth;
	}

	public int rows() {
		return mGridRows;
	}

	public int cols() {
		return mGridCols;
	}

	protected void mApplyGrid(FloatBuffer xyzPoints) {
		gridPoints(xyzPoints, 
                   mGridAvg, mGridCounts,
                   mGridWidth, mGridCols, 
                   mGridHeight, mGridRows);
	}

	protected void mScaleGridToBytes() {
		float mult = 255.0f / (mMaxDepth - mMinDepth);

		for(int i = 0; i < mGridImage.length; ++i) {
			int rawVal = (int)((mGridAvg[i] - mMinDepth) * mult);
			rawVal = Math.max(0, Math.min(255, rawVal));
			mGridImage[i] = (byte)rawVal;
		}
	}

	public String computeB64GridString(byte[] rawPointData) {
		FloatBuffer fpoints = decodeDepthBytes(rawPointData);

		mApplyGrid(fpoints);
		mScaleGridToBytes();

		return Base64.encodeToString(mGridImage, Base64.DEFAULT);
	}

    public FloatBuffer decodeDepthBytes(byte[] bbuffer) {
        FloatBuffer pointFloatBuff;
        pointFloatBuff = ByteBuffer.wrap(bbuffer).order(ByteOrder.nativeOrder()).asFloatBuffer();
        return pointFloatBuff;
    }

    public void gridPoints(FloatBuffer src, 
                            float[] dest_avg, int[] dest_ct, 
                            float xExtent, int xCells, 
                            float yExtent, int yCells) {
        int npoints = src.capacity() / 3;

        for(int i = 0; i < dest_avg.length; ++i) {
            dest_avg[i] = 0.0f;
            dest_ct[i] = 0;
        }

        float xmult = ((float)xCells) / (xExtent * 2.0f);
        float ymult = ((float)yCells) / (yExtent * 2.0f);

        for(int i = 0, idx = 0; i < npoints; ++i, idx += 3) {
            float x = src.get(idx    );
            float y = src.get(idx + 1);
            float z = src.get(idx + 2);
            if(z <= 0.0) { // this shouldn't happen but better to be safe
                continue;
            }
            float xprime = x / z;
            float yprime = y / z;
            if(xprime < xExtent && xprime > -xExtent &&
               yprime < yExtent && yprime > -yExtent) {
                int xi = (int)((xprime + xExtent) * xmult);
                int yi = (int)((yprime + yExtent) * ymult);
                dest_avg[xi + yi*xCells] += z;
                dest_ct[xi + yi*xCells] += 1;
            }
        }

        for(int i = 0; i < dest_avg.length; ++i) {
            if(dest_ct[i] > 0) {
                dest_avg[i] = dest_avg[i] / (float)(dest_ct[i]);
            }
        }
    }
}