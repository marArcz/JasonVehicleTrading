package com.example.jasonvehicletrading;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class VehicleTableHeaderAdapter extends de.codecrafters.tableview.TableHeaderAdapter {
    private final String headers[];
    private int paddingLeft = 20;
    private int paddingTop = 30;
    private int paddingRight = 20;
    private int paddingBottom = 30;
    private int textSize = 14;
    private int typeface = Typeface.BOLD;
    private int textColor = 0x99000000;
    private int gravity = Gravity.CENTER;

    protected VehicleTableHeaderAdapter(Context context, final String... headers) {
        super(context);
        this.headers = headers;
    }



    @Override
    public View getHeaderView(int columnIndex, ViewGroup parentView) {
        final TextView textView = new TextView(getContext());

        if (columnIndex < headers.length) {
            textView.setText(headers[columnIndex]);
            textView.setGravity(gravity);
        }

        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        textView.setTypeface(textView.getTypeface(), typeface);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        textView.setSingleLine(false);
        textView.setEllipsize(null);

        return textView;
    }
}
