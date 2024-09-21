package com.artifex.mupdfdemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.util.ArrayList;


interface TextProcessor {
    void onStartLine();

    void onWord(TextWord word);

    void onEndLine();


    void onEndText();
}

public abstract class PageView extends ViewGroup {
    private static final float ITEM_SELECT_BOX_WIDTH = 4.0f;
    private static final int HIGHLIGHT_COLOR = 0x80ade1f6;
    private static final int SELECTION_COLOR = 0x8033B5E5;
    private static final int SELECTION_MARKER_COLOR = 0xFF33B5E5;
    private static final int GRAYEDOUT_COLOR = 0x30000000;
    private static final int SEARCHRESULTS_COLOR = 0x3033B5E5;
    private static final int HIGHLIGHTED_SEARCHRESULT_COLOR = 0xFF33B5E5;
    private int LINK_COLOR;
    private static final int BOX_COLOR = -9868951;
    private int INK_COLOR;
    private float INK_THICKNESS;
    private float current_scale;
    private static final int BACKGROUND_COLOR = -1;
    private static final int PROGRESS_DIALOG_DELAY = 200;
    protected final Context mContext;
    protected int mPageNumber;
    private Point mParentSize;
    protected Point mSize;
    protected float mSourceScale;
    private ImageView mEntire;
    private Bitmap mEntireBm;
    private Matrix mEntireMat;
    private AsyncTask<Void, Void, TextWord[][]> mGetText;
    private AsyncTask<Void, Void, LinkInfo[]> mGetLinkInfo;
    private CancellableAsyncTask<Void, Void> mDrawEntire;
    private Point mPatchViewSize;
    private Rect mPatchArea;
    private ImageView mPatch;
    private Bitmap mPatchBm;
    private CancellableAsyncTask<Void, Void> mDrawPatch;
    private RectF[] mSearchBoxes;
    protected LinkInfo[] mLinks;
    public RectF mSelectBox;
    public RectF selectedText;
    private TextWord[][] mText;
    private RectF mItemSelectBox;
    protected ArrayList<ArrayList<PointF>> mDrawing;
    private View mSearchView;
    private boolean mIsBlank;
    private boolean mHighlightLinks;
    private ProgressBar mBusyIndicator;
    private final Handler mHandler;
    int once=-1;
    private final TextSelectionDrawer textSelectionDrawer = new TextSelectionDrawer();
    private PdfTextSelectionHelper textSelectionHelper = new PdfTextSelectionHelper();

    public PageView(final Context c, final Point parentSize, final Bitmap sharedHqBm) {
        super(c);
        this.LINK_COLOR = -2130749662;
        this.INK_COLOR = -16777216;
        this.INK_THICKNESS = 10.0f;
        this.mHandler = new Handler();
        this.mContext = c;
        this.mParentSize = parentSize;
        this.setBackgroundColor(-1);
        this.mEntireBm = Bitmap.createBitmap(parentSize.x, parentSize.y, Bitmap.Config.ARGB_8888);
        this.mPatchBm = sharedHqBm;
        this.mEntireMat = new Matrix();
    }

    protected abstract CancellableTaskDefinition<Void, Void> getDrawPageTask(final Bitmap p0, final int p1, final int p2, final int p3, final int p4, final int p5, final int p6);

    protected abstract CancellableTaskDefinition<Void, Void> getUpdatePageTask(final Bitmap p0, final int p1, final int p2, final int p3, final int p4, final int p5, final int p6);

    protected abstract LinkInfo[] getLinkInfo();

    protected abstract TextWord[][] getText();

    protected abstract void addMarkup(final PointF[] p0, final Annotation.Type p1 , int color);

    private void reinit() {
        reset();
        if (this.mDrawEntire != null) {
            this.mDrawEntire.cancelAndWait();
            this.mDrawEntire = null;
        }
        if (this.mDrawPatch != null) {
            this.mDrawPatch.cancelAndWait();
            this.mDrawPatch = null;
        }
        if (this.mGetLinkInfo != null) {
            this.mGetLinkInfo.cancel(true);
            this.mGetLinkInfo = null;
        }
        if (this.mGetText != null) {
            this.mGetText.cancel(true);
            this.mGetText = null;
        }
        this.mIsBlank = true;
        this.mPageNumber = 0;
        if (this.mSize == null) {
            this.mSize = this.mParentSize;
        }
        if (this.mEntire != null) {
            this.mEntire.setImageBitmap((Bitmap) null);
            this.mEntire.invalidate();
            Log.d("INVALIDATEunda","1111");
        }
        if (this.mPatch != null) {
            this.mPatch.setImageBitmap((Bitmap) null);
            this.mPatch.invalidate();
            Log.d("INVALIDATEunda","22222");

        }
        this.mPatchViewSize = null;
        this.mPatchArea = null;
        this.mSearchBoxes = null;
        this.mLinks = null;
        this.mSelectBox = null;
        this.mText = null;
        this.mItemSelectBox = null;

        Log.d("nullified","yes1");

        highlightedSearchResultPaint.setColor(HIGHLIGHTED_SEARCHRESULT_COLOR);
        highlightedSearchResultPaint.setStyle(Paint.Style.STROKE);
        highlightedSearchResultPaint.setAntiAlias(true);

        linksPaint.setColor(LINK_COLOR);
        linksPaint.setStyle(Paint.Style.STROKE);
        linksPaint.setStrokeWidth(0);

        selectBoxPaint.setColor(SELECTION_COLOR);
        selectBoxPaint.setStyle(Paint.Style.FILL);
        selectBoxPaint.setStrokeWidth(0);

        selectMarkerPaint.setColor(SELECTION_MARKER_COLOR);
        selectMarkerPaint.setStyle(Paint.Style.FILL);
        selectMarkerPaint.setStrokeWidth(0);

        selectOverlayPaint.setColor(GRAYEDOUT_COLOR);
        selectOverlayPaint.setStyle(Paint.Style.FILL);

        itemSelectBoxPaint.setColor(BOX_COLOR);
        itemSelectBoxPaint.setStyle(Paint.Style.STROKE);
        itemSelectBoxPaint.setStrokeWidth(3);

        drawingPaint.setAntiAlias(true);
        drawingPaint.setDither(true);
        drawingPaint.setStrokeJoin(Paint.Join.ROUND);
        drawingPaint.setStrokeCap(Paint.Cap.ROUND);
        drawingPaint.setStyle(Paint.Style.STROKE);

        eraserInnerPaint.setAntiAlias(true);
        eraserInnerPaint.setDither(true);
        eraserInnerPaint.setStyle(Paint.Style.FILL);
        eraserInnerPaint.setColor(ERASER_INNER_COLOR);

        eraserOuterPaint.setAntiAlias(true);
        eraserOuterPaint.setDither(true);
        eraserOuterPaint.setStyle(Paint.Style.STROKE);
        eraserOuterPaint.setColor(ERASER_OUTER_COLOR);
    }

    public void releaseResources() {
        this.reinit();

        if (this.mBusyIndicator != null) {
            this.removeView((View) this.mBusyIndicator);
            this.mBusyIndicator = null;
        }
    }
    private void reset() {
        if (mLoadTextTask != null) {
            mLoadTextTask.cancel(true);
            mLoadTextTask = null;
        }
    }

    public void releaseBitmaps() {
        this.reinit();
        if (this.mEntireBm != null) {
            this.mEntireBm.recycle();
        }
        this.mEntireBm = null;
        if (this.mPatchBm != null) {
            this.mPatchBm.recycle();
        }
        this.mPatchBm = null;
    }

    public void blank(final int page) {
        this.reinit();
        this.mPageNumber = page;
        if (this.mBusyIndicator == null) {
            (this.mBusyIndicator = new ProgressBar(this.mContext)).setIndeterminate(true);
            this.addView((View) this.mBusyIndicator);
        }
        this.setBackgroundColor(-1);
    }

    /* TODO: PageView::  this methods basically use to highlight pdf */
    public void setPage(final int page, final PointF size) {
        if (this.mDrawEntire != null) {
            this.mDrawEntire.cancelAndWait();
            this.mDrawEntire = null;
        }
        this.mIsBlank = false;
        if (this.mSearchView != null) {
            this.mSearchView.invalidate();
            Log.d("INVALIDATEunda","4444");

        }
        this.mPageNumber = page;
        if (this.mEntire == null) {
            (this.mEntire = (ImageView) new OpaqueImageView(this.mContext)).setScaleType(ImageView.ScaleType.MATRIX);
            this.addView((View) this.mEntire);
        }
        this.mSourceScale = Math.min(this.mParentSize.x / size.x, this.mParentSize.y / size.y);
        this.mSize = new Point((int) (size.x * this.mSourceScale), (int) (size.y * this.mSourceScale));
        this.mEntire.setImageBitmap((Bitmap) null);
        this.mEntire.invalidate();
        Log.d("INVALIDATEunda","5555");

        (this.mGetLinkInfo = new AsyncTask<Void, Void, LinkInfo[]>() {
            protected LinkInfo[] doInBackground(final Void... v) {
                return PageView.this.getLinkInfo();
            }

            protected void onPostExecute(final LinkInfo[] v) {
                PageView.this.mLinks = v;
                if (PageView.this.mSearchView != null) {
                    PageView.this.mSearchView.invalidate();
                    Log.d("INVALIDATEunda","31");

                }
            }
        }).execute(new Void[0]);
        (this.mDrawEntire = new CancellableAsyncTask<Void, Void>(this.getDrawPageTask(this.mEntireBm, this.mSize.x, this.mSize.y, 0, 0, this.mSize.x, this.mSize.y)) {
            @Override
            public void onPreExecute() {
                PageView.this.setBackgroundColor(-1);
                PageView.this.mEntire.setImageBitmap((Bitmap) null);
                PageView.this.mEntire.invalidate();
                Log.d("INVALIDATEunda","30");

                if (PageView.this.mBusyIndicator == null) {
                    PageView.this.mBusyIndicator = new ProgressBar(PageView.this.mContext);
                    PageView.this.mBusyIndicator.setIndeterminate(true);
                    PageView.this.addView((View) PageView.this.mBusyIndicator);
                    PageView.this.mBusyIndicator.setVisibility(4);
                    PageView.this.mHandler.postDelayed((Runnable) new Runnable() {
                        @Override
                        public void run() {
                            if (PageView.this.mBusyIndicator != null) {
                                PageView.this.mBusyIndicator.setVisibility(0);
                            }
                        }
                    }, 200L);
                }
            }

            @Override
            public void onPostExecute(final Void result) {
                PageView.this.removeView((View) PageView.this.mBusyIndicator);
                PageView.this.mBusyIndicator = null;
                PageView.this.mEntire.setImageBitmap(PageView.this.mEntireBm);
                PageView.this.mEntire.invalidate();
                Log.d("INVALIDATEunda","29");

                PageView.this.setBackgroundColor(0);
            }
        }).execute(new Void[0]);
        if (this.mSearchView == null) {
            this.addView(this.mSearchView = new View(this.mContext) {
//                private Float left = null;
//                private Float top = null;
//                private Float right = null;
//                private Float bottom = null;
//                private RectF rectF = null;
                RectF rectMain,rectMain2;
                private RectF lastCircleRect = null; // Track the last circle's position
                float initialHandleY2 = -1;  // Initialize it to an invalid value

                protected void onDraw(final Canvas canvas) {
                    super.onDraw(canvas);
                    final float scale = PageView.this.mSourceScale * this.getWidth() / PageView.this.mSize.x;
                    PageView.this.current_scale = scale;
                    final Paint paint = new Paint();
                    final Paint circlePaint = new Paint();
                    paint.setColor(Color.RED);
                    circlePaint.setColor(Color.BLUE);  // Circle color
                    circlePaint.setStyle(Paint.Style.FILL);
                    final RectF[] firstRect = {null};

                    // Draw the selection rectangle
                    if (PageView.this.mSelectBox != null && PageView.this.mText != null) {
          /*              int color = getInkColor();
                        paint.setColor(Color.argb(123, Color.red(color), Color.green(color), Color.blue(color)));
                        paint.setColor(HIGHLIGHT_COLOR);

                        final RectF[] lastLineRect = {null};

                        PageView.this.processSelectedText(new TextProcessor() {
                            RectF rect;
                            @Override
                            public void onStartLine() {
                                this.rect = new RectF();
                                if (firstRect[0] == null) {
                                    firstRect[0] = new RectF();
                                }
                            }

                            @Override
                            public void onWord(final TextWord word) {
                                this.rect.union((RectF) word);
                                if (firstRect[0].isEmpty()) {
                                    firstRect[0] = new RectF((RectF) word);  // Store the first word rect
                                }
                            }

                            @Override
                            public void onEndLine() {
                                if (!this.rect.isEmpty()) {
                                    // Store the current rect as the last rect
                                    lastLineRect[0] = new RectF(this.rect);

                                    // Draw the selection rectangle
                                    canvas.drawRect(this.rect.left * scale, this.rect.top * scale, this.rect.right * scale, this.rect.bottom * scale, paint);
                                }
                            }
                        });

                        // Draw a circle at the start of the selection
                        if (firstRect[0] != null) {
                            float startX = firstRect[0].left * scale;
                            float startY = (firstRect[0].top + firstRect[0].bottom) / 2 * scale;  // Midpoint of the first word's height
//                            canvas.drawCircle(startX, startY, 20f, circlePaint);  // Adjust the radius as needed
//                            textSelectionHelper.drawStartHandle();
                            float handleX = firstRect[0].left;  // You can adjust this to position on the left or right side
                            float handleY = (firstRect[0].top + firstRect[0].bottom) / 2;  // Midpoint of the top and bottom

                            // Call the drawStartHandle method with the calculated values
                            textSelectionHelper.drawStartHandle(canvas, handleX, handleY, scale);
                        }

                        // Draw a circle at the end of the selection
                        if (lastLineRect[0] != null) {
                            float endX = lastLineRect[0].right * scale;
                            float endY = (lastLineRect[0].top + lastLineRect[0].bottom) / 2 * scale;  // Midpoint of the last word's height
//                            canvas.drawCircle(endX, endY, 20f, circlePaint);  // Adjust the radius as needed
                            float handleRightX = lastLineRect[0].right;
                            float handleRightY = (lastLineRect[0].top + lastLineRect[0].bottom) / 2;
                            textSelectionHelper.drawEndHandle(canvas, handleRightX, handleRightY, scale);
                        }*/


                        textSelectionDrawer.reset(canvas, scale);
                        processSelectedText(textSelectionDrawer);
                    }
                }

/*
                protected void onDraw(final Canvas canvas) {
                    super.onDraw(canvas);
                    final float scale = PageView.this.mSourceScale * this.getWidth() / PageView.this.mSize.x;
                    PageView.this.current_scale = scale;
                    final Paint paint = new Paint();
                    final Paint paint2 = new Paint();
                    paint.setColor(Color.RED);

                    if (!PageView.this.mIsBlank && PageView.this.mSearchBoxes != null) {
                        paint.setColor(HIGHLIGHT_COLOR);
                        for (final RectF rect : PageView.this.mSearchBoxes) {
                            Log.d("chakka","yaaaa1111");

                            canvas.drawRect(rect.left * scale, rect.top * scale, rect.right * scale, rect.bottom * scale, paint);
                        }
                    }
                    if (!PageView.this.mIsBlank && PageView.this.mLinks != null && PageView.this.mHighlightLinks) {
                        paint.setColor(PageView.this.LINK_COLOR);
                        for (final LinkInfo link : PageView.this.mLinks) {
                            Log.d("chakka","yaaaa2222");

                            canvas.drawRect(link.rect.left * scale, link.rect.top * scale, link.rect.right * scale, link.rect.bottom * scale, paint);
                        }
                    }
                    if (PageView.this.mSelectBox != null && PageView.this.mText != null) {
                        int color = getInkColor();
                        paint.setColor(Color.argb(123, Color.red(color), Color.green(color), Color.blue(color)));
                        paint.setColor(HIGHLIGHT_COLOR);
                        Log.d("chakka","yaaaa33333");
                        final RectF[] lastLineRect = {null};
                        PageView.this.processSelectedText(new TextProcessor() {
                            RectF rect;
                            RectF lastRect; // Store the last line's RectF
                            @Override
                            public void onStartLine() {


                                this.rect = new RectF();
                                rectMain2=new RectF();
                                RectF lastRect; // Store the last line's RectF
                                if(once==0)
                                {
                                    rectMain=new RectF();

                                }

                            }
                            // Method to clear the handle from the previous location
                            private void clearPreviousHandle(Canvas canvas, RectF lastLineRect, float scale) {
                                if (lastLineRect != null) {
                                    float handleX = lastLineRect.right;
                                    float handleY = (lastLineRect.top + lastLineRect.bottom) / 2;
                                    float radius = 20f * scale; // Assuming the same radius used in drawStartHandle

                                    // Clear the area where the previous handle was drawn (use background color)
                                    Paint clearPaint = new Paint();
                                    clearPaint.setColor(Color.RED); // Set this to the canvas background color
                                    clearPaint.setStyle(Paint.Style.FILL);

                                    // Draw a circle over the previous handle to erase it
                                    canvas.drawCircle(handleX * scale, handleY * scale, radius, clearPaint);
                                }
                            }

                            @Override
                            public void onWord(final TextWord word) {
                                this.rect.union((RectF) word);
//                                if(once==0) {
                                    rectMain=((RectF) word);
//                                    once = 1;
//                                }
                            }

                            @Override
                            public void onEndLine() {
                                if (!this.rect.isEmpty()) {
                                    Log.d("chakka","yaaa4444");
                                    // Store the current rect as the last rect
                                    Log.d("check","mSelectBoxthis.rect>"+this.rect.left* scale+"<>"+this.rect.bottom* scale);

                                    lastLineRect[0] = new RectF(this.rect);
                                    canvas.drawRect(this.rect.left * scale, this.rect.top * scale, this.rect.right * scale, this.rect.bottom * scale, paint);
                                    canvas.drawCircle(rectMain.left * scale, (rectMain.top + rectMain.bottom) / 2 * scale, 20f, paint2);

//                                    textSelectionHelper.drawStartHandle(canvas,);
//                                    left = this.rect.left * scale;
//                                    top = this.rect.top * scale;
//                                    right = this.rect.right * scale;
//                                    bottom = this.rect.bottom * scale;
//                                    rectF = new RectF(left, top, right, bottom);
                                    // Define the radius of the circles
*/
/*                                    float circleRadius = 30f; // Adjust the size as needed

// Draw the left circle
                                    canvas.drawCircle(rectMain.left * scale, (rectMain.top + rectMain.bottom) / 2 * scale, circleRadius, paint2);

// Draw the right circle
                                    canvas.drawCircle(this.rect.right * scale, (this.rect.top + this.rect.bottom) / 2 * scale, circleRadius, paint2);*//*

//                                    if (lastRect != null) {
//                                        float circleRadius = 30f; // Adjust the size as needed
//
//                                        // Draw the circle only at the right of the last line
//                                        canvas.drawCircle(lastRect.right * scale, (lastRect.top + lastRect.bottom) / 2 * scale, circleRadius, paint2);
//
//                                        // Optionally, draw the circle at the left side of the first word in the first line
//                                        canvas.drawCircle(rectMain.left * scale, (rectMain.top + rectMain.bottom) / 2 * scale, circleRadius, paint2);
//                                    }
                                    // After processing all the lines, draw the circle only on the last line
//                                    if (lastLineRect[0] != null) {
//                                        float circleRadius = 30f; // Adjust the size as needed
//
//                                        // Draw the circle only on the right of the last line
//                                        canvas.drawCircle(lastLineRect[0].right * scale, (lastLineRect[0].top + lastLineRect[0].bottom) / 2 * scale, circleRadius, paint2);
//
//                                        // Optionally, draw the circle on the left side of the first word in the first line
//                                        canvas.drawCircle(rectMain.left * scale, (rectMain.top + rectMain.bottom) / 2 * scale, circleRadius, paint2);
//
//                                        // Update lastCircleRect with the current circle's position
//                                        lastCircleRect = new RectF(lastLineRect[0]);
//                                    }
//                                    if (lastCircleRect != null) {
//                                        float circleRadius = 30f;
//                                        canvas.drawCircle(lastCircleRect.right * scale, (lastCircleRect.top + lastCircleRect.bottom) / 2 * scale, circleRadius, paint);
//                                    }
//                                    selectedText = rect;
//                                    PageView.this.setItemSelectBox(rect);
                                    // Calculate the x and y positions for the handle
                                    */
/*float handleX = this.rect.left;  // You can adjust this to position on the left or right side
                                    float handleY = (this.rect.top + this.rect.bottom) / 2;  // Midpoint of the top and bottom

                                    float handleX2 = mSelectBox.left;  // You can adjust this to position on the left or right side
                                    float handleY2 = (mSelectBox.top + mSelectBox.bottom) / 2;  // Midpoint of the top and bottom
// If this is the first time drawing the handle, store the initial value
                                    if (initialHandleY2 == -1) {
                                        initialHandleY2 = (mSelectBox.top + mSelectBox.bottom) / 2;  // Calculate the midpoint
                                    }

// Use the stored initial value for handleY2, so it doesn't change on dragging
                                    handleY2 = initialHandleY2;
                                    // Call the drawStartHandle method with the calculated values
                                    textSelectionHelper.drawStartHandle(canvas, handleX2, handleY2, scale);  // Adjust the scale to act as zoom

                                    // Optionally draw the handle on the right of the last line
                                    if (lastLineRect[0] != null) {
                                        float handleRightX = lastLineRect[0].right;
                                        float handleRightY = (lastLineRect[0].top + lastLineRect[0].bottom) / 2;
                                        textSelectionHelper.drawEndHandle(canvas, handleRightX, handleRightY, scale);
                                    }*//*


                                }
                            }
                        });
                    }
                    if (PageView.this.mItemSelectBox != null) {
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(4.0f);
                        paint.setColor(BOX_COLOR);
                        Log.d("chakka","yaaaa55555");

                        canvas.drawRect(PageView.this.mItemSelectBox.left * scale, PageView.this.mItemSelectBox.top * scale, PageView.this.mItemSelectBox.right * scale, PageView.this.mItemSelectBox.bottom * scale, paint);
                    }
                    if (PageView.this.mDrawing != null) {
                        final Path path = new Path();
                        paint.setAntiAlias(true);
                        paint.setDither(true);
                        paint.setStrokeJoin(Paint.Join.ROUND);
                        paint.setStrokeCap(Paint.Cap.ROUND);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setStrokeWidth(PageView.this.INK_THICKNESS * scale);
                        paint.setColor(PageView.this.INK_COLOR);
                        for (final ArrayList<PointF> arc : PageView.this.mDrawing) {
                            if (arc.size() >= 2) {
                                final Iterator<PointF> iit = arc.iterator();
                                PointF p = iit.next();
                                float mX = p.x * scale;
                                float mY = p.y * scale;
                                path.moveTo(mX, mY);
                                while (iit.hasNext()) {
                                    p = iit.next();
                                    final float x = p.x * scale;
                                    final float y = p.y * scale;
                                    path.quadTo(mX, mY, (x + mX) / 2.0f, (y + mY) / 2.0f);
                                    mX = x;
                                    mY = y;
                                }
                                path.lineTo(mX, mY);
                            } else {
                                final PointF p = arc.get(0);
                                canvas.drawCircle(p.x * scale, p.y * scale, PageView.this.INK_THICKNESS * scale / 2.0f, paint);
                            }
                        }
                        paint.setStyle(Paint.Style.STROKE);
                        canvas.drawPath(path, paint);
                        Log.d("chakka","yaaaa66666");

                    }
                }
*/
            });
        }
        loadText();
        this.requestLayout();
    }

    public void setSearchBoxes(final RectF[] searchBoxes) {
        this.mSearchBoxes = searchBoxes;
        if (this.mSearchView != null) {
            this.mSearchView.invalidate();
            Log.d("INVALIDATEunda","27");

        }
    }

    public void setLinkHighlighting(final boolean f) {
        this.mHighlightLinks = f;
        if (this.mSearchView != null) {
            this.mSearchView.invalidate();
            Log.d("INVALIDATEunda","26");

        }
    }

    public void setLinkHighlightColor(final int color) {
        this.LINK_COLOR = color;
        if (this.mHighlightLinks && this.mSearchView != null) {
            this.mSearchView.invalidate();
            Log.d("INVALIDATEunda","25");

        }
    }

    public void deselectText() {
        this.mSelectBox = null;
        Log.d("nullified","yes2");

        this.mSearchView.invalidate();
        Log.d("INVALIDATEunda","24");

    }
    private float rectSize = 50f; // Size of the RectF
    private RectF startPoint;
    public void  selectorFirstPoint(final float x, final float y)
    {
        startPoint = new RectF(
                x - rectSize / 2, // left
                y - rectSize / 2, // top
                x + rectSize / 2, // right
                y + rectSize / 2  // bottom
        );
        once=0;
    }

    public void resetSelection()
    {
        once=-1;
    }


    int k=0;
    // Define a tolerance for detecting clicks near edges or corners
    private static final float EDGE_TOLERANCE = 100.0f; // Adjust this value as needed

    // Method to check click position relative to the RectF
    boolean isLeft=false;
    SelectorMode selmode=SelectorMode.IDLEMODE;
    private void checkClickPosition(float clickX, float clickY, RectF rect) {
        // Calculate the corners of the RectF
        float left = rect.left;
        float top = rect.top;
        float right = rect.right;
        float bottom = rect.bottom;

        // Check if the click is near the right bottom corner
        if (Math.abs(clickX - right) <= EDGE_TOLERANCE && Math.abs(clickY - bottom) <= EDGE_TOLERANCE) {
            System.out.println("Clicked near the right bottom corner");
            isLeft=false;
            selmode=SelectorMode.DRAGGINGRIGHT;
        }
        // Check if the click is near the left top corner
        else if (Math.abs(clickX - left) <= EDGE_TOLERANCE && Math.abs(clickY - top) <= EDGE_TOLERANCE) {
            System.out.println("Clicked near the left top corner");
            isLeft=true;
            selmode=SelectorMode.DRAGGINGLEFT;

        }
        // Check if the click is near any other edge (optional)
        else if (Math.abs(clickX - left) <= EDGE_TOLERANCE || Math.abs(clickX - right) <= EDGE_TOLERANCE ||
                Math.abs(clickY - top) <= EDGE_TOLERANCE || Math.abs(clickY - bottom) <= EDGE_TOLERANCE) {
            if(isLeft)
            {
                System.out.println("Clicked near an edge after LeftClick");

            }else {
                System.out.println("Clicked near an edge after Right");

            }

            if(selmode==SelectorMode.IDLEMODE)
            {
                selmode=SelectorMode.DRAGGINGNEWAREA;
            }else   if(selmode==SelectorMode.DRAGGINGLEFT)
            {
                selmode=SelectorMode.DRAGGINGLEFT;
            }
            else  if(selmode==SelectorMode.DRAGGINGRIGHT)
            {
                selmode=SelectorMode.DRAGGINGRIGHT;
            }
        }
        else {

            if(isLeft)
            {
                System.out.println("Clicked inside the RectF but not near edges LEFT");

            }else {
                System.out.println("Clicked inside the RectF but not near edges RIGHT");

            }

            if(selmode==SelectorMode.IDLEMODE)
            {
                selmode=SelectorMode.DRAGGINGNEWAREA;
            }else   if(selmode==SelectorMode.DRAGGINGLEFT)
            {
                selmode=SelectorMode.DRAGGINGSELCTIONLTR;
            }
            else  if(selmode==SelectorMode.DRAGGINGRIGHT)
            {
                selmode=SelectorMode.DRAGGINGSELCTIONRTL;
            }
        }
    }

    // Example usage
    public void onUserClick(float clickX, float clickY) {
        RectF myRect = new RectF(229.5f, 611.2064f, 686.6694f, 769.499f);
        checkClickPosition(clickX, clickY, myRect);
    }
public void selectEvent(MotionEvent e)
{
    Log.d("Clikmode","EVENy"+e.getAction());
    if(e.getAction()==MotionEvent.ACTION_UP)
    {
        selmode=SelectorMode.IDLEMODE;
    }

}

    public void selectText(float x0, float y0, float x1, float y1) {
        float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
        float docRelX0 = (x0 - getLeft())/scale;
        float docRelY0 = (y0 - getTop())/scale;
        float docRelX1 = (x1 - getLeft())/scale;
        float docRelY1 = (y1 - getTop())/scale;

        // Order on Y but maintain the point grouping
        if (docRelY0 <= docRelY1)
            mSelectBox = new RectF(docRelX0, docRelY0, docRelX1, docRelY1);
        else
            mSelectBox = new RectF(docRelX1, docRelY1, docRelX0, docRelY0);

        //Adjust the min/max x values between which text is selected
        if(Math.max(docRelX0,docRelX1)>docRelXmax) docRelXmax = Math.max(docRelX0,docRelX1);
        if(Math.min(docRelX0,docRelX1)<docRelXmin) docRelXmin = Math.min(docRelX0,docRelX1);

        mSearchView.invalidate();

        loadText(); //We should do this earlier in the background ...
    }
    private       AsyncTask<Void,Void,TextWord[][]> mLoadTextTask;

    private void loadText() {
        if (mLoadTextTask == null) {
            mLoadTextTask = new AsyncTask<Void,Void,TextWord[][]>() {
                @Override
                protected TextWord[][] doInBackground(Void... params) {
                    return getText();
                }
                @Override
                protected void onPostExecute(TextWord[][] result) {
                    mText = result;
                    mSearchView.invalidate();
                }
            };
            mLoadTextTask.execute();
        }
    }

    public void selectdText(final float x0, final float y0, final float x1, final float y1) {


        float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
        float docRelX0 = (x0 - getLeft())/scale;
        float docRelY0 = (y0 - getTop())/scale;
        float docRelX1 = (x1 - getLeft())/scale;
        float docRelY1 = (y1 - getTop())/scale;

        // Order on Y but maintain the point grouping
        if (docRelY0 <= docRelY1)
            mSelectBox = new RectF(docRelX0, docRelY0, docRelX1, docRelY1);
        else
            mSelectBox = new RectF(docRelX1, docRelY1, docRelX0, docRelY0);

        //Adjust the min/max x values between which text is selected
        if(Math.max(docRelX0,docRelX1)>docRelXmax) docRelXmax = Math.max(docRelX0,docRelX1);
        if(Math.min(docRelX0,docRelX1)<docRelXmin) docRelXmin = Math.min(docRelX0,docRelX1);


//        final float scale = this.mSourceScale * this.getWidth() / this.mSize.x;
//        final float docRelX0 = (x0 - this.getLeft()) / scale;
//        final float docRelY0 = (y0 - this.getTop()) / scale;
        final float docRelX2 = (x1 - this.getLeft()) / scale;
        final float docRelY2 = (y1 - this.getTop()) / scale;
        RectF newSelectBox;
        if (docRelY0 <= docRelY2) {
            newSelectBox = new RectF(docRelX0, docRelY0, docRelX2, docRelY2);
        } else {
            newSelectBox = new RectF(docRelX2, docRelY2, docRelX0, docRelY0);
        }




        // If mSelectBox is already defined, merge it with the new select box
        if (this.mSelectBox != null) {

            this.mSelectBox=expandOriginalRect(mSelectBox,newSelectBox);

//            checkTouchInRect(mSelectBox,docRelX2,docRelY2);

//                this.mSelectBox.union(newSelectBox);


        } else {

            this.mSelectBox = newSelectBox;  // First selection

        }
//        if (docRelY0 <= docRelY2) {
//            this.mSelectBox = new RectF(docRelX0, docRelY0, docRelX2, docRelY2);
//        } else {
//            this.mSelectBox = new RectF(docRelX2, docRelY2, docRelX0, docRelY0);
//        }
        this.mSearchView.invalidate();
        Log.d("INVALIDATEunda","23");

        if (this.mGetText == null) {
            (this.mGetText = new AsyncTask<Void, Void, TextWord[][]>() {
                protected TextWord[][] doInBackground(final Void... params) {
                    return PageView.this.getText();
                }

                protected void onPostExecute(final TextWord[][] result) {
                    PageView.this.mText = result;
                    PageView.this.mSearchView.invalidate();
                    Log.d("INVALIDATEunda","22");


                }
            }).execute(new Void[0]);
        }
    }
    public  RectF expandOriginalRect(RectF originalRect, RectF newRect)  {
        // Check if newRect is within originalRect
        boolean isLeftTopInside = originalRect.contains(newRect.left, newRect.top);
        boolean isLeftBottomInside = originalRect.contains(newRect.left, newRect.bottom);

        // If left-top and left-bottom of newRect are inside originalRect
        // but newRect's left side is outside
        if (isLeftTopInside && isLeftBottomInside && newRect.left > originalRect.left) {
            // Expand the right side of originalRect to cover newRect
            originalRect.right = Math.max(originalRect.right, newRect.right);
            return originalRect;
        }
        RectF unionRect = new RectF(originalRect);
        unionRect.union(newRect);
        return newRect;
    }
    final float sideMargin = 100f;  // Adjust as necessary for sensitivity

    public void checkTouchInRect(RectF rect, float touchX, float touchY) {
        // First, check if the touch is inside the rectangle
        if (rect.contains(touchX, touchY)) {
            // Now determine which part of the rect was touched
            if (touchX >= rect.left && touchX <= rect.left + sideMargin) {
                // Touched near the left side
                Log.d("TouchEvent", "Touch near the left side of the rectangle");
            } else if (touchX <= rect.right && touchX >= rect.right - sideMargin) {
                // Touched near the right side
                Log.d("TouchEvent", "Touch near the right side of the rectangle");
            } else {
                // Touched inside, but not near the sides
                Log.d("TouchEvent", "Touch inside the rectangle but not near the sides");
            }
        } else {
            // Touch is outside the rectangle
            Log.d("TouchEvent", "Touch outside the rectangle");
        }
    }
    public void startDraw(final float x, final float y) {
        final float scale = this.mSourceScale * this.getWidth() / this.mSize.x;
        final float docRelX = (x - this.getLeft()) / scale;
        final float docRelY = (y - this.getTop()) / scale;
        if (this.mDrawing == null) {
            this.mDrawing = new ArrayList<ArrayList<PointF>>();
        }
        final ArrayList<PointF> arc = new ArrayList<PointF>();
        arc.add(new PointF(docRelX, docRelY));
        this.mDrawing.add(arc);
        this.mSearchView.invalidate();
        Log.d("INVALIDATEunda","21");

    }

    public void continueDraw(final float x, final float y) {
        final float scale = this.mSourceScale * this.getWidth() / this.mSize.x;
        final float docRelX = (x - this.getLeft()) / scale;
        final float docRelY = (y - this.getTop()) / scale;
        if (this.mDrawing != null && this.mDrawing.size() > 0) {
            final ArrayList<PointF> arc = this.mDrawing.get(this.mDrawing.size() - 1);
            arc.add(new PointF(docRelX, docRelY));
            this.mSearchView.invalidate();
            Log.d("INVALIDATEunda","20");

        }
    }

    public void cancelDraw() {
        this.mDrawing = null;
        this.mSearchView.invalidate();
        Log.d("INVALIDATEunda","15");

    }

    protected PointF[][] getDraw() {
        if (this.mDrawing == null) {
            return null;
        }
        final PointF[][] path = new PointF[this.mDrawing.size()][];
        for (int i = 0; i < this.mDrawing.size(); ++i) {
            final ArrayList<PointF> arc = this.mDrawing.get(i);
            path[i] = arc.toArray(new PointF[arc.size()]);
        }
        return path;
    }

    public void setInkColor(final int color) {
        this.INK_COLOR = color;
    }

    public void setPaintStrockWidth(final float inkThickness) {
        this.INK_THICKNESS = inkThickness;
    }

    protected float getInkThickness() {
        if (this.current_scale == 0.0f) {
            return 4.537815f;
        }
        return this.INK_THICKNESS / 2.0f;
    }

    public float getCurrentScale() {
        if (this.current_scale == 0.0f) {
            return 9.07563f;
        }
        return this.current_scale;
    }

    protected float[] getColor() {
        return this.changeColor(this.INK_COLOR);
    }

    protected int getInkColor() {
        return this.INK_COLOR;
    }

    private float[] changeColor(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        float colors[] = new float[3];
        colors[0] = red / 255f;
        colors[1] = green / 255f;
        colors[2] = blue / 255f;
        return colors;
    }

    private float[] changeAnnotationColor(int color) {

        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        float colors[] = new float[3];
        colors[0] = red / 255f;
        colors[1] = green / 255f;
        colors[2] = blue / 255f;

        return colors;
    }
    protected void processSelectedText(TextProcessor tp) {
        Log.d("dddddd","CheckprocessSelectedText"+mText+"<><><"+mSelectBox);

        if (useSmartTextSelection)
            (new TextSelector(mText, mSelectBox,docRelXmin,docRelXmax)).select(tp);
        else
            (new TextSelector(mText, mSelectBox)).select(tp);
    }

   /* protected void processSelectedText(TextProcessor tp) {
        (new TextSelector(mText, mSelectBox)).select(tp);
        TextSelectionDrawer textSelectionDrawer = new TextSelectionDrawer();
        textSelectionDrawer.reset(canvas, scale); // Make sure to pass the correct canvas and scale
        new TextSelector(mText, mSelectBox).select(textSelectionDrawer);
    }*/

    public void setItemSelectBox(final RectF rect) {
        this.mItemSelectBox = rect;
        if (this.mSearchView != null) {
            this.mSearchView.invalidate();
            Log.d("INVALIDATEunda","14");

        }
    }

    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int x = 0;
        switch (MeasureSpec.getMode(widthMeasureSpec)) {
            case 0: {
                x = this.mSize.x;
                break;
            }
            default: {
                x = MeasureSpec.getSize(widthMeasureSpec);
                break;
            }
        }
        int y = 0;
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case 0: {
                y = this.mSize.y;
                break;
            }
            default: {
                y = MeasureSpec.getSize(heightMeasureSpec);
                break;
            }
        }
        this.setMeasuredDimension(x, y);
        if (this.mBusyIndicator != null) {
            final int limit = Math.min(this.mParentSize.x, this.mParentSize.y) / 2;
            this.mBusyIndicator.measure(Integer.MIN_VALUE | limit, Integer.MIN_VALUE | limit);
        }
    }

    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        final int w = right - left;
        final int h = bottom - top;
        if (this.mEntire != null) {
            if (this.mEntire.getWidth() != w || this.mEntire.getHeight() != h) {
                this.mEntireMat.setScale(w / (float) this.mSize.x, h / (float) this.mSize.y);
                this.mEntire.setImageMatrix(this.mEntireMat);
                this.mEntire.invalidate();
                Log.d("INVALIDATEunda","13");

            }
            this.mEntire.layout(0, 0, w, h);
        }
        if (this.mSearchView != null) {
            this.mSearchView.layout(0, 0, w, h);
        }
        if (this.mPatchViewSize != null) {
            if (this.mPatchViewSize.x != w || this.mPatchViewSize.y != h) {
                this.mPatchViewSize = null;
                this.mPatchArea = null;
                if (this.mPatch != null) {
                    this.mPatch.setImageBitmap((Bitmap) null);
                    this.mPatch.invalidate();
                    Log.d("INVALIDATEunda","12");

                }
            } else {
                this.mPatch.layout(this.mPatchArea.left, this.mPatchArea.top, this.mPatchArea.right, this.mPatchArea.bottom);
            }
        }
        if (this.mBusyIndicator != null) {
            final int bw = this.mBusyIndicator.getMeasuredWidth();
            final int bh = this.mBusyIndicator.getMeasuredHeight();
            this.mBusyIndicator.layout((w - bw) / 2, (h - bh) / 2, (w + bw) / 2, (h + bh) / 2);
        }
    }

    public void updateHq(final boolean update) {
        final Rect viewArea = new Rect(this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
        if (viewArea.width() == this.mSize.x || viewArea.height() == this.mSize.y) {
            if (this.mPatch != null) {
                this.mPatch.setImageBitmap((Bitmap) null);
                this.mPatch.invalidate();
                Log.d("INVALIDATEunda","eleven");

            }
        } else {
            final Point patchViewSize = new Point(viewArea.width(), viewArea.height());
            final Rect patchArea = new Rect(0, 0, this.mParentSize.x, this.mParentSize.y);
            if (!patchArea.intersect(viewArea)) {
                return;
            }
            patchArea.offset(-viewArea.left, -viewArea.top);
            final boolean area_unchanged = patchArea.equals(this.mPatchArea) && patchViewSize.equals(this.mPatchViewSize);
            if (area_unchanged && !update) {
                return;
            }
            final boolean completeRedraw = !area_unchanged;
            if (this.mDrawPatch != null) {
                this.mDrawPatch.cancelAndWait();
                this.mDrawPatch = null;
            }
            if (this.mPatch == null) {
                (this.mPatch = (ImageView) new OpaqueImageView(this.mContext)).setScaleType(ImageView.ScaleType.MATRIX);
                this.addView((View) this.mPatch);
                this.mSearchView.bringToFront();
            }
            CancellableTaskDefinition<Void, Void> task;
            if (completeRedraw) {
                task = this.getDrawPageTask(this.mPatchBm, patchViewSize.x, patchViewSize.y, patchArea.left, patchArea.top, patchArea.width(), patchArea.height());
            } else {
                task = this.getUpdatePageTask(this.mPatchBm, patchViewSize.x, patchViewSize.y, patchArea.left, patchArea.top, patchArea.width(), patchArea.height());
            }
            (this.mDrawPatch = new CancellableAsyncTask<Void, Void>(task) {
                @Override
                public void onPostExecute(final Void result) {
                    PageView.this.mPatchViewSize = patchViewSize;
                    PageView.this.mPatchArea = patchArea;
                    PageView.this.mPatch.setImageBitmap(PageView.this.mPatchBm);
                    PageView.this.mPatch.invalidate();
                    Log.d("INVALIDATEunda","ten");

                    PageView.this.mPatch.layout(PageView.this.mPatchArea.left, PageView.this.mPatchArea.top, PageView.this.mPatchArea.right, PageView.this.mPatchArea.bottom);
                }
            }).execute(new Void[0]);
        }
    }

    public void update() {
        if (this.mDrawEntire != null) {
            this.mDrawEntire.cancelAndWait();
            this.mDrawEntire = null;
        }
        if (this.mDrawPatch != null) {
            this.mDrawPatch.cancelAndWait();
            this.mDrawPatch = null;
        }
        (this.mDrawEntire = new CancellableAsyncTask<Void, Void>(this.getUpdatePageTask(this.mEntireBm, this.mSize.x, this.mSize.y, 0, 0, this.mSize.x, this.mSize.y)) {
            @Override
            public void onPostExecute(final Void result) {
                PageView.this.mEntire.setImageBitmap(PageView.this.mEntireBm);
                PageView.this.mEntire.invalidate();
                Log.d("INVALIDATEunda","99999");

            }
        }).execute(new Void[0]);
        this.updateHq(true);
    }

    public void removeHq() {
        if (this.mDrawPatch != null) {
            this.mDrawPatch.cancelAndWait();
            this.mDrawPatch = null;
        }
        this.mPatchViewSize = null;
        this.mPatchArea = null;
        if (this.mPatch != null) {
            this.mPatch.setImageBitmap((Bitmap) null);
            this.mPatch.invalidate();
            Log.d("INVALIDATEunda","88888");

        }
    }

    public int getPage() {
        return this.mPageNumber;
    }

    public boolean isOpaque() {
        return true;
    }
    private final Paint searchResultPaint = new Paint();
    private final Paint highlightedSearchResultPaint = new Paint();
    private final Paint linksPaint = new Paint();
    private final Paint selectBoxPaint = new Paint();
    private final Paint selectMarkerPaint = new Paint();
    private final Paint selectOverlayPaint = new Paint();
    private final Paint itemSelectBoxPaint = new Paint();
    private final Paint drawingPaint = new Paint();
    private final Paint eraserInnerPaint = new Paint();
    private final Paint eraserOuterPaint = new Paint();
    class TextSelectionDrawer implements TextProcessor {
        RectF rect;
        RectF firstLineRect = new RectF();
        RectF lastLineRect = new RectF();
        Path leftMarker = new Path();
        Path rightMarker = new Path();
        float height;
        float oldHeight = 0f;
        float docRelXmaxSelection = Float.NEGATIVE_INFINITY;
        float docRelXminSelection = Float.POSITIVE_INFINITY;
        float scale;
        Canvas canvas;

        public void reset(Canvas canvas, float scale) {
            this.canvas = canvas;
            this.scale = scale;
            firstLineRect.setEmpty();
            lastLineRect.setEmpty();
            docRelXmaxSelection = Float.NEGATIVE_INFINITY;
            docRelXminSelection = Float.POSITIVE_INFINITY;
        }

        public void onStartLine() {
            rect = new RectF();
        }

        public void onWord(TextWord word) {
            rect.union(word);
        }

        public void onEndLine() {
            if (!rect.isEmpty()) {
                if (firstLineRect.isEmpty() || firstLineRect.top > rect.top) {
                    firstLineRect.set(rect);
                }
                if (lastLineRect.isEmpty() || lastLineRect.bottom < rect.bottom) {
                    lastLineRect.set(rect);
                }

                canvas.drawRect(rect.left * scale, rect.top * scale,
                        rect.right * scale, rect.bottom * scale, selectBoxPaint);

                docRelXmaxSelection = Math.max(docRelXmaxSelection, Math.max(rect.right, docRelXmax));
                docRelXminSelection = Math.min(docRelXminSelection, Math.min(rect.left, docRelXmin));
            }
           /* if (!rect.isEmpty())
            {
                if(firstLineRect == null || firstLineRect.top > rect.top)
                {
                    if(firstLineRect == null) firstLineRect = new RectF();
                    firstLineRect.set(rect);
                }
                if(lastLineRect == null || lastLineRect.bottom < rect.bottom)
                {
                    if(lastLineRect == null) lastLineRect = new RectF();
                    lastLineRect.set(rect);
                }


                canvas.drawRect(rect.left*scale, rect.top*scale, rect.right*scale, rect.bottom*scale, selectBoxPaint);

                docRelXmaxSelection = Math.max(docRelXmaxSelection,Math.max(rect.right,docRelXmax));
                docRelXminSelection = Math.min(docRelXminSelection,Math.min(rect.left,docRelXmin));
            }*/
        }

        public void onEndText() {
           /* if (!firstLineRect.isEmpty() && !lastLineRect.isEmpty()) {
                height = Math.min(Math.max(Math.max(firstLineRect.bottom - firstLineRect.top,
                                        lastLineRect.bottom - lastLineRect.top),
                                getResources().getDisplayMetrics().xdpi * 0.07f / scale),
                        4 * getResources().getDisplayMetrics().xdpi * 0.07f / scale);
                leftMarkerRect.set(firstLineRect.left-0.9f*height,firstLineRect.top,firstLineRect.left,firstLineRect.top+1.9f*height);
                rightMarkerRect.set(lastLineRect.right,lastLineRect.top,lastLineRect.right+0.9f*height,lastLineRect.top+1.9f*height);

                leftMarker.reset();
                leftMarker.moveTo(0f, 0f);
                leftMarker.lineTo(0f, 1.9f * height * scale);
                leftMarker.lineTo(-0.9f * height * scale, 1.9f * height * scale);
                leftMarker.close();

                rightMarker.reset();
                rightMarker.moveTo(0f, 0f);
                rightMarker.lineTo(0f, 1.9f * height * scale);
                rightMarker.lineTo(0.9f * height * scale, 1.9f * height * scale);
                rightMarker.close();

                leftMarker.offset(firstLineRect.left * scale, firstLineRect.top * scale);
                rightMarker.offset(lastLineRect.right * scale, lastLineRect.top * scale);
                canvas.drawPath(leftMarker, selectMarkerPaint);
                canvas.drawPath(rightMarker, selectMarkerPaint);
            }*/
           /*
            if(firstLineRect != null && lastLineRect != null)
            {
                height = Math.min(Math.max(Math.max(firstLineRect.bottom - firstLineRect.top, lastLineRect.bottom - lastLineRect.top), getResources().getDisplayMetrics().xdpi*0.07f/scale), 4*getResources().getDisplayMetrics().xdpi*0.07f/scale);

                leftMarkerRect.set(firstLineRect.left-0.9f*height,firstLineRect.top,firstLineRect.left,firstLineRect.top+1.9f*height);
                rightMarkerRect.set(lastLineRect.right,lastLineRect.top,lastLineRect.right+0.9f*height,lastLineRect.top+1.9f*height);

                if(height != oldHeight || true)
                {
                    leftMarker.rewind();
                    leftMarker.moveTo(0f,0f);
                    leftMarker.rLineTo(0f,1.9f*height*scale);
                    leftMarker.rLineTo(-0.9f*height*scale,0f);
                    leftMarker.rLineTo(0f,-0.9f*height*scale);
                    leftMarker.close();

                    rightMarker.rewind();
                    rightMarker.moveTo(0f,0f);
                    rightMarker.rLineTo(0f,1.9f*height*scale);
                    rightMarker.rLineTo(0.9f*height*scale,0f);
                    rightMarker.rLineTo(0f,-0.9f*height*scale);
                    rightMarker.close();
                    oldHeight = height;
                }
Log.d("ckckckc","firstLineRect"+firstLineRect);
                Log.d("ckckckc","last"+lastLineRect);

                leftMarker.offset(firstLineRect.left*scale, firstLineRect.top*scale);
                rightMarker.offset(lastLineRect.right*scale, lastLineRect.top*scale);

//                textSelectionHelper.drawStartHandle(canvas,firstLineRect.left,firstLineRect.top,scale);
//                textSelectionHelper.drawEndHandle(canvas,lastLineRect.right,lastLineRect.top,scale);

                canvas.drawPath(leftMarker, selectMarkerPaint);
                canvas.drawPath(rightMarker, selectMarkerPaint);
                //Undo the offset so that we can reuse the path
                leftMarker.offset(-firstLineRect.left*scale, -firstLineRect.top*scale);
                rightMarker.offset(-lastLineRect.right*scale, -lastLineRect.top*scale);
            }

            if (useSmartTextSelection) {
                canvas.drawRect(0, 0, docRelXminSelection * scale, PageView.this.getHeight(), selectOverlayPaint);
                canvas.drawRect(docRelXmaxSelection * scale, 0, PageView.this.getWidth(), PageView.this.getHeight(), selectOverlayPaint);
            }

            */
            if (firstLineRect != null && lastLineRect != null) {
                height = Math.min(Math.max(Math.max(firstLineRect.bottom - firstLineRect.top, lastLineRect.bottom - lastLineRect.top), getResources().getDisplayMetrics().xdpi * 0.07f / scale), 4 * getResources().getDisplayMetrics().xdpi * 0.07f / scale);

                leftMarkerRect.set(firstLineRect.left - 0.9f * height, firstLineRect.top, firstLineRect.left, firstLineRect.top + 1.9f * height);
                rightMarkerRect.set(lastLineRect.right, lastLineRect.top, lastLineRect.right + 0.9f * height, lastLineRect.top + 1.9f * height);

                /*if (height != oldHeight || true) {
                    float cornerRadius = 0.4f * height * scale; // Adjust this value for corner curvature

                    // Left marker with rounded bottom
                    leftMarker.rewind();
                    leftMarker.moveTo(0f, 0f);
                    leftMarker.rLineTo(0f, 1.5f * height * scale);  // Straight line down
                    leftMarker.quadTo(-0.9f * height * scale / 2, 1.9f * height * scale, -0.9f * height * scale, 1.5f * height * scale);  // Rounded bottom
                    leftMarker.rLineTo(0f, -1.5f * height * scale);  // Straight line up
                    leftMarker.close();

                    // Right marker with rounded bottom
                    rightMarker.rewind();
                    rightMarker.moveTo(0f, 0f);
                    rightMarker.rLineTo(0f, 1.5f * height * scale);  // Straight line down
                    rightMarker.quadTo(0.9f * height * scale / 2, 1.9f * height * scale, 0.9f * height * scale, 1.5f * height * scale);  // Rounded bottom
                    rightMarker.rLineTo(0f, -1.5f * height * scale);  // Straight line up
                    rightMarker.close();

                    oldHeight = height;
                }*/

                Log.d("ckckckc", "firstLineRect" + firstLineRect);
                Log.d("ckckckc", "last" + lastLineRect);

//                leftMarker.offset(firstLineRect.left * scale, firstLineRect.top * scale);
//                rightMarker.offset(lastLineRect.right * scale, lastLineRect.top * scale);

                // Drawing the paths
//                canvas.drawPath(leftMarker, selectMarkerPaint);
//                canvas.drawPath(rightMarker, selectMarkerPaint);
                textSelectionHelper.drawStartHandle(canvas,firstLineRect.left,firstLineRect.top,scale);
                textSelectionHelper.drawEndHandle(canvas,lastLineRect.right,lastLineRect.top,scale);
                // Undo the offset so that we can reuse the path
//                leftMarker.offset(-firstLineRect.left * scale, -firstLineRect.top * scale);
//                rightMarker.offset(-lastLineRect.right * scale, -lastLineRect.top * scale);
            }

            if (useSmartTextSelection) {
                canvas.drawRect(0, 0, docRelXminSelection * scale, PageView.this.getHeight(), selectOverlayPaint);
                canvas.drawRect(docRelXmaxSelection * scale, 0, PageView.this.getWidth(), PageView.this.getHeight(), selectOverlayPaint);
            }


        }
    }
    private static boolean useSmartTextSelection = false;
    private       float     docRelXmax = Float.NEGATIVE_INFINITY;
    private       float     docRelXmin = Float.POSITIVE_INFINITY;

    //Update in following TextSelectionDrawer (coordinates are relative to document)
    private RectF leftMarkerRect = new RectF();
    private RectF rightMarkerRect= new RectF();

    public boolean hitsLeftMarker(float x, float y)
    {
        float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
        float docRelX = (x - getLeft())/scale;
        float docRelY = (y - getTop())/scale;
        Log.d("LADALALA","MMleftMarkerRect"+leftMarkerRect);
        return leftMarkerRect != null && leftMarkerRect.contains(docRelX,docRelY);
    }
    public boolean hitsRightMarker(float x, float y)
    {
        float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
        float docRelX = (x - getLeft())/scale;
        float docRelY = (y - getTop())/scale;
        Log.d("LADALALA","MMrightMarkerRect"+rightMarkerRect);

        return rightMarkerRect != null && rightMarkerRect.contains(docRelX,docRelY);
    }
    public void moveLeftMarker(MotionEvent e){
        float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
        float docRelX = (e.getX() - getLeft())/scale;
        float docRelY = (e.getY() - getTop())/scale;

        mSelectBox.left=docRelX;
        if(docRelY < mSelectBox.bottom)
            mSelectBox.top=docRelY;
        else {
            mSelectBox.top=mSelectBox.bottom;
            mSelectBox.bottom=docRelY;
        }
        if(docRelX>docRelXmax) docRelXmax = docRelX;
        if(docRelX<docRelXmin) docRelXmin = docRelX;
        mSearchView.invalidate();
    }

    public void moveRightMarker(MotionEvent e){
        float scale = mSourceScale*(float)getWidth()/(float)mSize.x;
        float docRelX = (e.getX() - getLeft())/scale;
        float docRelY = (e.getY() - getTop())/scale;
        mSelectBox.right=docRelX;
        if(docRelY > mSelectBox.top)
            mSelectBox.bottom=docRelY;
        else {
            mSelectBox.bottom=mSelectBox.top;
            mSelectBox.top=docRelY;
        }
        if(docRelX>docRelXmax) docRelXmax = docRelX;
        if(docRelX<docRelXmin) docRelXmin = docRelX;
        mSearchView.invalidate();
    }
    private static final int ERASER_INNER_COLOR = 0xFFFFFFFF;
    private static final int ERASER_OUTER_COLOR = 0xFF000000;

    public boolean hasTextSelected() {
Log.d("dddddd","Check");
        class Boolean {
            public boolean value;
        }

        final Boolean b = new Boolean();
        b.value = false;

        processSelectedText(new TextProcessor() {
            public void onStartLine() {}
            public void onWord(TextWord word) {
                b.value = true;
            }
            public void onEndLine() {}
            public void onEndText() {}
        });
        return b.value;
    }

}
