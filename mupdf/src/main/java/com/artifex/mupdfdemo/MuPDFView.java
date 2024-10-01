//
// Decompiled by Procyon v0.5.36
//

package com.artifex.mupdfdemo;

import android.graphics.RectF;
import android.graphics.PointF;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

public interface MuPDFView
{
    void setPage(final int p0, final PointF p1);

    void setScale(final float p0);

    int getPage();

    void blank(final int p0);

    Hit passClickEvent(final float p0, final float p1);
    public void moveRightMarker(MotionEvent e);
    public boolean hitsRightMarker(float x, float y);
    public void moveLeftMarker(MotionEvent e);
    public boolean hitsLeftMarker(float x, float y);

    LinkInfo hitLink(final float p0, final float p1);

    void selectText(final float p0, final float p1, final float p2, final float p3);
    void selectEvent(MotionEvent e);

    void selectorFirstPoint(final float p0, final float p1);

    void resetSelection();


    void deselectText();

    boolean copySelection();
    boolean isTextSelected();

    String getTextSelectedArea();

    ArrayList<PointF> markupSelection(final Annotation.Type p0);

    boolean markupFromDbSelection(final Annotation.Type p0, List<PointF> quadPoints);


    void deleteSelectedAnnotation();

    void setSearchBoxes(final RectF[] p0);

    RectF getRectToDelete();

    void clearDeleteRect();

    void setLinkHighlighting(final boolean p0);

    void deselectAnnotation();

    void startDraw(final float p0, final float p1);

    void continueDraw(final float p0, final float p1);

    void cancelDraw();

    PointF[][] saveDraw();


    boolean saveDrawFromDb(PointF[][] points);

    void setChangeReporter(final Runnable p0);

    void update();

    void updateHq(final boolean p0);

    void removeHq();

    void releaseResources();

    void releaseBitmaps();

    void setLinkHighlightColor(final int p0);

    void setInkColor(final int p0);

    void setPaintStrockWidth(final float p0);

    float getCurrentScale();
    void showCopyRect(float x, float y);

}
