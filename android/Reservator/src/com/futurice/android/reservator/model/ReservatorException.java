package com.futurice.android.reservator.model;

public class ReservatorException extends Exception {
	private static final long serialVersionUID = 1L;

	public ReservatorException(String detailMessage) {
		super(detailMessage);
	}

	public ReservatorException(Throwable throwable) {
		super(throwable);
	}

	public ReservatorException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}
}
