package com.xilidou.serivce;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.xilidou.Constants;
import com.xilidou.entity.ApiResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhengxinaa
 */
@Service
public class FormatService {

	public List<String> getTranslations(ApiResponse apiResponse, int status){
		List<String> translations = apiResponse.getTranslation();
		List<String> subList = translations.subList(0, 1);
		return subList.parallelStream().map(t->formatString(t,status)).collect(Collectors.toList());
	}

	public String formatString(String str,int status){
		String underline = CharMatcher.whitespace().trimAndCollapseFrom(str,'_');
		underline = CharMatcher.inRange('a','z')
				.or(CharMatcher.inRange('A','Z'))
				.or(CharMatcher.is('_'))
				.retainFrom(underline);
		if(underline.startsWith("_")){
			underline = underline.substring(1);
		}

		switch (status){
			case Constants.LOWER_CAMEL:
				return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,underline);
			case Constants.LOWER_HYPHEN:
				return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN,underline);
			case Constants.LOWER_UNDERSCORE:
				return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE,underline);
			case Constants.UPPER_CAMEL:
				return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL,underline);
			case Constants.UPPER_UNDERSCORE:
				return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_UNDERSCORE,underline);
			default:
				return  CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL,underline);
		}

	}

	public String getWord(ApiResponse apiResponse){
		if(apiResponse.getErrorCode() == 0 && apiResponse.getBasic() != null){
			return apiResponse.getBasic().getExplains().get(0);
		}

		else {
			return apiResponse.getTranslation().get(0);
		}
	}

}
