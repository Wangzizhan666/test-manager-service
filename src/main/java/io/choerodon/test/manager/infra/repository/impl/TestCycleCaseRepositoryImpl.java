package io.choerodon.test.manager.infra.repository.impl;

import java.util.*;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import com.github.pagehelper.PageInfo;

import io.choerodon.core.convertor.ConvertHelper;
import io.choerodon.core.convertor.ConvertPageHelper;
import io.choerodon.core.exception.CommonException;
import io.choerodon.base.domain.PageRequest;
import io.choerodon.test.manager.domain.repository.TestCycleCaseRepository;
import io.choerodon.test.manager.domain.test.manager.entity.TestCycleCaseE;
import io.choerodon.test.manager.infra.common.utils.DBValidateUtil;
import io.choerodon.test.manager.infra.common.utils.LiquibaseHelper;
import io.choerodon.test.manager.infra.dataobject.TestCycleCaseDO;
import io.choerodon.test.manager.infra.exception.TestCycleCaseException;
import io.choerodon.test.manager.infra.mapper.TestCycleCaseMapper;

/**
 * Created by 842767365@qq.com on 6/11/18.
 */
@Component
public class TestCycleCaseRepositoryImpl implements TestCycleCaseRepository {
    @Autowired
    TestCycleCaseMapper testCycleCaseMapper;

    @Value("${spring.datasource.url}")
    private String dsUrl;

    @Override
    public TestCycleCaseE insert(TestCycleCaseE testCycleCaseE) {
        TestCycleCaseDO convert = ConvertHelper.convert(testCycleCaseE, TestCycleCaseDO.class);
        DBValidateUtil.executeAndvalidateUpdateNum(testCycleCaseMapper::insert, convert, 1, "error.testCycleCase.insert");
        return ConvertHelper.convert(convert, TestCycleCaseE.class);
    }

    @Override
    public void delete(TestCycleCaseE testCycleCaseE) {
        TestCycleCaseDO convert = ConvertHelper.convert(testCycleCaseE, TestCycleCaseDO.class);
        testCycleCaseMapper.delete(convert);
    }

    @Override
    public TestCycleCaseE update(TestCycleCaseE testCycleCaseE) {
        if (testCycleCaseE.getProjectId() == null) {
            throw new CommonException("error.projectId.illegal");
        }
        TestCycleCaseDO convert = ConvertHelper.convert(testCycleCaseE, TestCycleCaseDO.class);
        DBValidateUtil.executeAndvalidateUpdateNum(testCycleCaseMapper::updateByPrimaryKey, convert, 1, "error.testCycleCase.update");
        return ConvertHelper.convert(testCycleCaseMapper.selectByPrimaryKey(convert.getExecuteId()), TestCycleCaseE.class);
    }

    @Override
    public PageInfo<TestCycleCaseE> query(TestCycleCaseE testCycleCaseE, PageRequest pageRequest) {
        TestCycleCaseDO convert = ConvertHelper.convert(testCycleCaseE, TestCycleCaseDO.class);
        List<TestCycleCaseDO> dto = queryWithAttachAndDefect(convert, pageRequest);
//        Long total = 0L;
//        if (dto != null && !dto.isEmpty()) {
//            total = testCycleCaseMapper.queryWithAttachAndDefect_count(convert);
//        }
//        PageInfo info = new PageInfo(pageRequest.getPage(), pageRequest.getSize());
        PageInfo<TestCycleCaseDO> page = new PageInfo<>(Optional.ofNullable(dto).orElseGet(ArrayList::new));
        return ConvertPageHelper.convertPageInfo(page, TestCycleCaseE.class);
    }

    @Override
    public PageInfo<TestCycleCaseE> queryByFatherCycle(List<TestCycleCaseE> testCycleCaseES, PageRequest pageRequest) {
        List<TestCycleCaseDO> converts = ConvertHelper.convertList(testCycleCaseES, TestCycleCaseDO.class);
        List<TestCycleCaseDO> dtos = queryByFatherCycleWithDataBase(converts, pageRequest);
        Long total = 0L;
        for (TestCycleCaseDO convert : converts) {
            total += testCycleCaseMapper.queryWithAttachAndDefect_count(convert);
        }
        if (dtos.isEmpty() && total != 0L) {
            pageRequest.setPage((total.intValue() / pageRequest.getSize()) - 1);
            dtos = queryByFatherCycleWithDataBase(converts, pageRequest);
        }
//        PageInfo info = new PageInfo(pageRequest.getPage(), pageRequest.getSize());
        Page page = new Page<>(pageRequest.getPage(), pageRequest.getSize());
        page.setTotal(total);
        page.addAll(dtos);
        return ConvertPageHelper.convertPageInfo(page.toPageInfo(), TestCycleCaseE.class);
    }

    private List<TestCycleCaseDO> queryByFatherCycleWithDataBase(List<TestCycleCaseDO> converts, PageRequest pageRequest) {
//        switch (LiquibaseHelper.dbType(dsUrl)) {
//            case MYSQL:
//            case H2:
//                return PageHelper.doSort(pageRequest.getSort(), () -> testCycleCaseMapper.queryByFatherCycleWithAttachAndDefect(converts, pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize()));
//            case ORACLE:
//                return PageHelper.doSort(pageRequest.getSort(), () -> testCycleCaseMapper.queryByFatherCycleWithAttachAndDefect_oracle(converts, pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize()));
//            default:
//                throw new TestCycleCaseException(TestCycleCaseException.ERROR_UN_SUPPORT_DB_TYPE + ",need mysql or oracle but now is:" + dsUrl);
//        }
//        PageInfo p = PageHelper.startPage(pageRequest.getPage(), pageRequest.getSize(), pageRequest.getSort().toSql()).doSelectPageInfo(() -> testCycleCaseMapper.queryByFatherCycleWithAttachAndDefect(converts, pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize()));
        List a = testCycleCaseMapper.queryByFatherCycleWithAttachAndDefect(converts, (pageRequest.getPage() - 1) * pageRequest.getSize(), pageRequest.getSize());
        return a;
    }

    @Override
    public List<TestCycleCaseE> query(TestCycleCaseE testCycleCaseE) {
        TestCycleCaseDO convert = ConvertHelper.convert(testCycleCaseE, TestCycleCaseDO.class);
        return ConvertHelper.convertList(testCycleCaseMapper.select(convert), TestCycleCaseE.class);
    }

    private List<TestCycleCaseDO> queryWithAttachAndDefect(TestCycleCaseDO convert, PageRequest pageRequest) {
//        switch (LiquibaseHelper.dbType(dsUrl)) {
//            case MYSQL:
//            case H2:
//                return testCycleCaseMapper.queryWithAttachAndDefect(convert, pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize());
//            case ORACLE:
//                return testCycleCaseMapper.queryWithAttachAndDefect_oracle(convert, pageRequest.getPage() * pageRequest.getSize(), pageRequest.getSize());
//            default:
//                throw new TestCycleCaseException(TestCycleCaseException.ERROR_UN_SUPPORT_DB_TYPE + ",need mysql or oracle but now is:" + dsUrl);
//        }
        return testCycleCaseMapper.queryWithAttachAndDefect(convert, (pageRequest.getPage() - 1) * pageRequest.getSize(), pageRequest.getSize());
    }

    @Override
    public TestCycleCaseE queryOne(TestCycleCaseE testCycleCaseE) {
        TestCycleCaseDO convert = ConvertHelper.convert(testCycleCaseE, TestCycleCaseDO.class);

        List<TestCycleCaseDO> list = queryWithAttachAndDefect(convert, new PageRequest(1, 1));
        DBValidateUtil.executeAndvalidateUpdateNum(list::size, 1, "error.cycle.case.query.not.found");
        return ConvertHelper.convert(list.get(0), TestCycleCaseE.class);
    }

    @Override
    public List<TestCycleCaseE> filter(Map map) {
        return ConvertHelper.convertList(testCycleCaseMapper.filter(map), TestCycleCaseE.class);
    }

    @Override
    public List<TestCycleCaseE> queryByIssue(Long issueId) {

        return ConvertHelper.convertList(testCycleCaseMapper.queryByIssue(issueId), TestCycleCaseE.class);

    }

    @Override
    public List<TestCycleCaseE> queryInIssue(Long[] issueId) {
        Assert.notEmpty(issueId, "erorr.query.cycle.in.issues.issueIds.not.null");
        return ConvertHelper.convertList(testCycleCaseMapper.queryInIssues(issueId), TestCycleCaseE.class);

    }

    /**
     * 查询versions下所有的Case
     *
     * @param
     * @return
     */
    @Override
    public List<TestCycleCaseE> queryCaseAllInfoInCyclesOrVersions(Long[] cycleIds, Long[] versionIds) {
        if (!(ObjectUtils.isEmpty(cycleIds) ^ ObjectUtils.isEmpty(versionIds))) {
            Assert.notEmpty(cycleIds, "erorr.query.cycle.in.issues.issueIds.not.null");
        }
        return ConvertHelper.convertList(testCycleCaseMapper.queryCaseAllInfoInCyclesOrVersions(cycleIds, versionIds), TestCycleCaseE.class);

    }

    @Override
    public List<TestCycleCaseE> queryCycleCaseForReporter(Long[] issueIds) {
        return ConvertHelper.convertList(testCycleCaseMapper.queryCycleCaseForReporter(issueIds), TestCycleCaseE.class);

    }

    @Override
    public Long countCaseNotRun(Long[] cycleIds) {
        return testCycleCaseMapper.countCaseNotRun(cycleIds);
    }

    @Override
    public Long countCaseNotPlain(Long[] cycleIds) {
        return testCycleCaseMapper.countCaseNotPlain(cycleIds);

    }

    @Override
    public Long countCaseSum(Long[] cycleIds) {
        return testCycleCaseMapper.countCaseSum(cycleIds);

    }

    @Override
    public void validateCycleCaseInCycle(TestCycleCaseDO testCycleCase) {
        if (testCycleCaseMapper.validateCycleCaseInCycle(testCycleCase).longValue() > 0) {
            throw new CommonException("error.cycle.case.insert.have.one.case.in.cycle");
        }
    }

    @Override
    public String getLastedRank(Long cycleId) {
        return LiquibaseHelper.executeFunctionByMysqlOrOracle(testCycleCaseMapper::getLastedRank, testCycleCaseMapper::getLastedRank_oracle, dsUrl, cycleId);

    }

    @Override
    public List<TestCycleCaseE> batchInsert(Long projectId, List<TestCycleCaseE> testCycleCases) {
        if (testCycleCases == null || testCycleCases.isEmpty()) {
            throw new CommonException("error.cycle.case.list.empty");
        }
        Date now = new Date();
        for (TestCycleCaseE testCycleCaseE : testCycleCases) {
            if (testCycleCaseE == null || testCycleCaseE.getExecuteId() != null) {
                throw new CommonException("error.cycle.case.insert.executeId.should.be.null");
            }
            testCycleCaseE.setCreationDate(now);
            testCycleCaseE.setLastUpdateDate(now);
            testCycleCaseE.setProjectId(projectId);
        }

        List<TestCycleCaseDO> testCycleCaseDOs = ConvertHelper.convertList(testCycleCases, TestCycleCaseDO.class);
        DBValidateUtil.executeAndvalidateUpdateNum(
                testCycleCaseMapper::batchInsertTestCycleCases, testCycleCaseDOs, testCycleCaseDOs.size(), "error.testCycleCase.batchInsert");

        return ConvertHelper.convertList(testCycleCaseDOs, TestCycleCaseE.class);
    }

}
