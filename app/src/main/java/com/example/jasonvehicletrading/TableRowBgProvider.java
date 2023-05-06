package com.example.jasonvehicletrading;

import android.content.Context;
import android.graphics.drawable.Drawable;

import de.codecrafters.tableview.providers.TableDataRowBackgroundProvider;

public class TableRowBgProvider implements TableDataRowBackgroundProvider {
    private Context context;

    public TableRowBgProvider(Context context) {
        this.context = context;
    }
    @Override
    public Drawable getRowBackground(int rowIndex, Object rowData) {
        return context.getResources().getDrawable(rowIndex % 2 == 0 ? R.drawable.odd_row_bg : R.drawable.even_row_bg);
    }
}
