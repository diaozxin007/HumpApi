package com.xilidou.serivce;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zhengxin
 */
@Component
@Slf4j
public class CounterService {

	@Value("${counter.objectid}")
	private String objectId;

	public void add(int amount){
		AVObject avObject = AVObject.createWithoutData("Counter",objectId);
		avObject.increment("views",amount);
		avObject.setFetchWhenSave(true);

		try {
			avObject.save();
		} catch (AVException e) {
			log.info("add counter error",e);
		}
	}

}
