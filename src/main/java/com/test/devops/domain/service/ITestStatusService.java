package com.test.devops.domain.service;

import com.test.devops.domain.entity.TestStatusE;

import java.util.List;

/**
 * Created by jialongZuo@hand-china.com on 6/25/18.
 */
public interface ITestStatusService {
	List<TestStatusE> query(TestStatusE testStatusE);

	TestStatusE insert(TestStatusE testStatusE);

	void delete(TestStatusE testStatusE);

	TestStatusE update(TestStatusE testStatusE);
}
