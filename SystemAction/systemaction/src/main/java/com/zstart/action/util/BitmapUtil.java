package com.zstart.action.util;

import android.R;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;

import com.zstart.action.FastBitmapDrawable;

public final class BitmapUtil {
	public static final int DEFAULT_TEXTURE_WIDTH = 400;
	public static final int DEFAULT_TEXTURE_HEIGHT = 225;
	
	public static int TEXTURE_WIDTH = DEFAULT_TEXTURE_WIDTH;
	public static int TEXTURE_HEIGHT = DEFAULT_TEXTURE_HEIGHT;

	private static final Rect sOldBounds = new Rect();
	private static final Canvas sCanvas = new Canvas();

	static {
		sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
				Paint.FILTER_BITMAP_FLAG));
	}
	static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
	static int sColorIndex = 0;

	public static Drawable createIconDrawable(Bitmap icon) {
		FastBitmapDrawable d = new FastBitmapDrawable(icon);
		d.setFilterBitmap(true);
		resizeIconDrawable(d);
		return d;
	}

	public static void resizeIconDrawable(Drawable icon) {
		icon.setBounds(0, 0, TEXTURE_WIDTH, TEXTURE_HEIGHT);
	}

	public static Bitmap createIconBitmap(Bitmap icon, Context context) {
		int textureWidth = TEXTURE_WIDTH;
		int textureHeight = TEXTURE_HEIGHT;
		int sourceWidth = icon.getWidth();
		int sourceHeight = icon.getHeight();
		if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
			// Icon is bigger than it should be; clip it (solves the GB->ICS
			// migration case)
			return Bitmap.createBitmap(icon, (sourceWidth - textureWidth) / 2,
					(sourceHeight - textureHeight) / 2, textureWidth,
					textureHeight);
		} else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
			// Icon is the right size, no need to change it
			return icon;
		} else {
			// Icon is too small, render to a larger bitmap
			final Resources resources = context.getResources();
			return createIconBitmap(new BitmapDrawable(resources, icon),
					context);
		}
	}

	public static Bitmap createIconBitmap(Drawable icon, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (TEXTURE_HEIGHT == -1) {
				initStatics(context);
			}
  
			int width = TEXTURE_HEIGHT;
			int height = TEXTURE_HEIGHT;

			if (icon instanceof PaintDrawable) {
				PaintDrawable painter = (PaintDrawable) icon;
				painter.setIntrinsicWidth(width);
				painter.setIntrinsicHeight(height);
			} else if (icon instanceof BitmapDrawable) {
				// Ensure the bitmap has a density.
				BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
				Bitmap bitmap = bitmapDrawable.getBitmap();
				if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
					bitmapDrawable.setTargetDensity(context.getResources()
							.getDisplayMetrics());
				}
			}
			int sourceWidth = icon.getIntrinsicWidth();
			int sourceHeight = icon.getIntrinsicHeight();
			if(sourceWidth > 0 && sourceHeight > 0){
				float ratio = TEXTURE_HEIGHT / (float)sourceHeight;
				if(sourceHeight > TEXTURE_HEIGHT){
					width = (int)(sourceWidth * ratio);
					height = TEXTURE_HEIGHT;
				}else{
					float whRatio = sourceHeight / (float)sourceWidth;
					if(whRatio > 0.99 && whRatio < 1.01){
						width = TEXTURE_HEIGHT;
						height = TEXTURE_HEIGHT;
					}else{
						width = sourceWidth;
						height = sourceHeight;
					}
				}
			}
			
			/*if (sourceWidth > 0 && sourceHeight > 0) {
				// Scale the icon proportionally to the icon dimensions
				final float ratio = (float) sourceWidth / sourceHeight;
				if (sourceWidth > sourceHeight) {
					height = (int) (width / ratio);
				} else if (sourceHeight > sourceWidth) {
					width = (int) (height * ratio);
				}
			}*/

			// no intrinsic size --> use default size
			//LogUtil.w("createIconBitmap---"+width + "---"+height);
			int textureWidth = width;
			int textureHeight = height;

			final Bitmap bitmap = Bitmap.createBitmap(textureWidth,
					textureHeight, Bitmap.Config.ARGB_8888);
			final Canvas canvas = sCanvas;
			canvas.setBitmap(bitmap);

			final int left = (textureWidth - width) / 2;
			final int top = (textureHeight - height) / 2;

			@SuppressWarnings("all")
			// suppress dead code warning
			final boolean debug = false;
			if (debug) {
				// draw a big box for the icon for debugging
				canvas.drawColor(sColors[sColorIndex]);
				if (++sColorIndex >= sColors.length)
					sColorIndex = 0;
				Paint debugPaint = new Paint();
				debugPaint.setColor(0xffcccc00);
				canvas.drawRect(left, top, left + width, top + height,
						debugPaint);
			}

			sOldBounds.set(icon.getBounds());
			icon.setBounds(left, top, left + width, top + height);
			icon.draw(canvas);
			icon.setBounds(sOldBounds);
			canvas.setBitmap(null);

			return bitmap;
		}
	}

	static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
		synchronized (sCanvas) { // we share the statics :-(
			if (TEXTURE_WIDTH == -1) {
				initStatics(context);
			}

			if (bitmap.getWidth() == TEXTURE_WIDTH
					&& bitmap.getHeight() == TEXTURE_HEIGHT) {
				return bitmap;
			} else {
				final Resources resources = context.getResources();
				return createIconBitmap(new BitmapDrawable(resources, bitmap),
						context);
			}
		}
	}

	public static void initStatics(Context context) {
		final Resources resources = context.getResources();
		int width = (int) resources.getDimension(R.dimen.app_icon_size);
		TEXTURE_WIDTH = TEXTURE_HEIGHT = width;
	}

	public static void setIconSize(int widthPx) {
		TEXTURE_WIDTH = TEXTURE_HEIGHT = widthPx;
	}
}
