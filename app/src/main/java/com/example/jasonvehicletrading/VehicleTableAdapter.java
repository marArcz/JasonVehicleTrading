package com.example.jasonvehicletrading;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import de.codecrafters.tableview.TableDataAdapter;

public class VehicleTableAdapter extends TableDataAdapter<Vehicle> {

    private int paddingLeft = 20;
    private int paddingTop = 25;
    private int paddingBottom = 25;

    private int paddingRight = 20;
    private int textSize = 14;
    private int typeface = Typeface.NORMAL;
    private int textColor = 0x99000000;
    private int gravity = Gravity.START;

    public VehicleTableAdapter(Context context, List<Vehicle> data) {
        super(context, data);
    }

    @Override
    public Vehicle getRowData(int rowIndex) {
        return super.getRowData(rowIndex);
    }


    @Override
    public View getCellView(int rowIndex, int columnIndex, ViewGroup parentView) {
        Vehicle vehicle = getItem(rowIndex);
        TextView textView = new TextView(getContext());
        textView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        textView.setTypeface(textView.getTypeface(), typeface);
        textView.setTextSize(textSize);
        textView.setTextColor(textColor);
        textView.setGravity(Gravity.CENTER);
        textView.setSingleLine(false);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
        String cellContent = "";
        switch (columnIndex){
            case 0:
                cellContent = "";
                if(vehicle.isIs_sold()) textView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.sold_signage),null,null,null);
                break;
            case 1:
                cellContent = String.valueOf(vehicle.getContainer_number());
                break;
            case 2:
                cellContent = vehicle.getUnit();
                break;
            case 3:
                cellContent = String.valueOf(vehicle.getYear_model());
                break;
            case 4:
                cellContent = vehicle.getSpecs();
                break;
            case 5:
                cellContent = vehicle.getPrice();
                break;
            case 6:
                cellContent = vehicle.getUpdated_price();
                break;
        }
        textView.setText(cellContent);

        return textView;
    }

}
