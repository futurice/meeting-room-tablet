package com.futurice.android.reservator.view;

import com.futurice.android.reservator.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TableRow;

public class ReserveView extends TableRow{

	public ReserveView(Context context) {
		super(context);
		inflate(getContext(), R.layout.reserve, this);
	}
	public ReserveView(Context context, AttributeSet attrs){
		super(context,attrs);
	}

}
