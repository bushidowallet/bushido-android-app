package com.bitcoin.blockchain.api.android.barcode;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;

public class ScanResult {

	private Result rawResult;
	private ParsedResult parsedResult;

	public ScanResult(ParsedResult parseResult, Result rawResult) {
		this.rawResult = rawResult;
		this.parsedResult = parseResult;
	}

	public Result getRawResult() {
		return rawResult;
	}

	public void setRawResult(Result rawResult) {
		this.rawResult = rawResult;
	}

	public ParsedResult getParsedResult() {
		return parsedResult;
	}

	public void setParsedResult(ParsedResult parsedResult) {
		this.parsedResult = parsedResult;
	}
}
