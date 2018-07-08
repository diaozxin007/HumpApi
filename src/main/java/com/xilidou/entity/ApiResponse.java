package com.xilidou.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ApiResponse {

	private int errorCode;

	private String query;

	private List<String> translation;

	private ApiBasic basic;

//	private String web;

//	private String l;

//	private List<String> dict;

//	private String webdict;

	private String tSpeakUrl;

	private String speakUrl;

}
