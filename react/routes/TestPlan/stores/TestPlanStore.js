import {
  observable, action, computed, toJS,
} from 'mobx';
import { Choerodon, stores } from '@choerodon/boot';

import { getStatusList } from '@/api/TestStatusApi';
import {
  getExecutesByFolder, getStatusByFolder, getPlanDetail, executesAssignTo,
} from '@/api/TestPlanApi';
import TestPlanTreeStore from './TestPlanTreeStore';

const { AppState } = stores;
class TestPlanStore extends TestPlanTreeStore {
    @observable loading = true;

    @action setLoading = (loading) => {
      this.loading = loading;
    }

    @computed get getLoading() {
      return this.loading;
    }

    // @observable mainActiveTab = this.isPlan(this.currentCycle.id) ? 'testPlanSchedule' : 'testPlanTable';
    @observable mainActiveTab = 'testPlanSchedule';

    @action setMainActiveTab = (mainActiveTab) => {
      this.mainActiveTab = mainActiveTab;
    }

    @observable tableLoading = false;

    @action setTableLoading = (tableLoading) => {
      this.tableLoading = tableLoading;
    }

    @observable calendarLoading = false;

    @action setCalendarLoading = (calendarLoading) => {
      this.calendarLoading = calendarLoading;
    };

    @computed get getTableLoading() {
      return this.tableLoading;
    }

    @observable executePagination = {
      current: 1,
      total: 0,
      pageSize: 20,
    };

    @action setExecutePagination(executePagination) {
      this.executePagination = { ...this.executePagination, ...executePagination };
    }

    @computed get getExecutePagination() {
      return toJS(this.executePagination);
    }

    @observable statusList = [];

    @action setStatusList(statusList) {
      this.statusList = statusList;
    }

    @observable testList = [];

    @action setTestList = (testList) => {
      this.testList = testList;
    }

    @computed get getTestList() {
      return toJS(this.testList);
    }

    @observable filter = {};

    @action setFilter = (filter) => {
      this.filter = filter;
    }

    @observable barFilter = [];

    @action setBarFilter = (barFilter) => {
      this.barFilter = barFilter;
    }

    @observable order = {
      orderField: '',
      orderType: '',
    };

    @computed get getSearchObj() {
      return {
        searchArgs: this.filter,
        contents: this.barFilter,
      };
    }

    @computed get getFilters() {
      return this.filter;
    }

    // 我的执行
    @observable mineExecutePagination = {
      current: 1,
      total: 0,
      pageSize: 20,
    };

    @action setMineExecutePagination(mineExecutePagination) {
      this.mineExecutePagination = { ...this.mineExecutePagination, ...mineExecutePagination };
    }

    @computed get getMineExecutePagination() {
      return toJS(this.mineExecutePagination);
    }

    @observable mineFilter = {};

    @action setMineFilter = (mineFilter) => {
      this.mineFilter = mineFilter;
    }

    @observable mineBarFilter = [];

    @action setMineBarFilter = (mineBarFilter) => {
      this.mineBarFilter = mineBarFilter;
    }

    @observable mineOrder = {
      mineOrderField: '',
      mineOrderType: '',
    };

    @computed get getMineSearchObj() {
      return {
        searchArgs: { ...this.mineFilter, ...{ assignUser: AppState.userInfo.id } },
        contents: this.mineBarFilter,
      };
    }

    @computed get getMineFilters() {
      return this.mineFilter;
    }

    @observable assignToUserId;

    @action setAssignToUserId = (assignToUserId) => {
      this.assignToUserId = assignToUserId;
    }

    @observable comparedInfo = {};

    @action setComparedInfo = (comparedInfo) => {
      this.comparedInfo = comparedInfo;
    }

    checkIdMap = observable.map();

    @observable planInfo = {};

    @action setPlanInfo = (planInfo) => {
      this.planInfo = planInfo;
    }

    @observable statusRes = {};

    @action setStatusRes = (statusRes) => {
      this.statusRes = statusRes;
    }

    @action clearStore = () => {
      this.tableLoading = false;
      this.treeData = {};
      this.testList = [];
      this.expandedKeys = ['0-0'];
      this.selectedKeys = [];
      this.currentCycle = {};
      this.preCycle = {};
      this.filter = {};
      this.barFilter = [];
      this.executePagination = {
        current: 1,
        total: 0,
        pageSize: 20,
      };
      this.checkIdMap = observable.map();
      this.order = {
        orderField: '',
        orderType: '',
      };
      this.testPlanStatus = 'todo';
      this.planInfo = {};
      this.statusRes = {};
      this.mainActiveTab = 'testPlanSchedule';
      this.times = [];
    }

    /**
     * 加载左边的树和右边的表格
     *
     * @memberof TestPlanStore
     */
    loadAllData = () => {
      this.setLoading(true);
      return Promise.all([getStatusList('CYCLE_CASE'), this.loadIssueTree()]).then(([statusList]) => {
        this.setLoading(false);
        this.setStatusList(statusList);
        if (this.getCurrentPlanId) {
          this.loadPlanDetail();
          this.loadExecutes();
          this.loadStatusRes();
        }
      }).catch((e) => {
        Choerodon.prompt(e.message);
        this.setLoading(false);
      });
    }

    loadRightData = async (planId, folderId) => {
      const promiseArr = [this.loadExecutes(planId, folderId), this.loadStatusRes(planId, folderId)];
      if (planId !== this.getCurrentPlanId) {
        promiseArr.push(this.loadPlanDetail(planId));
      }
      Promise.all(promiseArr).then(() => {
      }).catch((e) => {
        // console.log(e);
      });
    }

    /**
     * 加载测试计划的表格
     *
     * @memberof TestPlanStore
     */
    async loadExecutes(planId = this.getId()[0], folderId = this.getId()[1], isMine = false) {
      const {
        executePagination, mineExecutePagination, order, mineOrder, getSearchObj, getMineSearchObj,
      } = this;
      this.setTableLoading(true);
      let executes = {};
      if (!isMine) {
        const { orderField, orderType } = order;
        const { current, pageSize } = executePagination;
        const search = getSearchObj;
        executes = await getExecutesByFolder({
          planId, folderId, current, pageSize, search, orderField, orderType,
        });
        this.setExecutePagination({
          ...executePagination,
          ...{ total: executes.total },
        });
      } else {
        const { mineOrderField, mineOrderType } = mineOrder;
        const { current, pageSize } = mineExecutePagination;
        const mineSearch = getMineSearchObj;
        executes = await getExecutesByFolder({
          planId, folderId, current, pageSize, search: mineSearch, orderField: mineOrderField, orderType: mineOrderType,
        });
        
        this.setMineExecutePagination({
          ...mineExecutePagination,
          ...{ total: executes.total },
        });
        console.log({
          ...mineExecutePagination,
          ...{ total: executes.total },
        });
      }
      this.setTestList(executes.list || []);
      this.setTableLoading(false);
    }

    /**
     * 加载当前层级执行状态统计
     *
     * @memberof TestPlanStore
     */
    loadStatusRes(planId = this.getId()[0], folderId = this.getId()[1]) {
      getStatusByFolder({ planId, folderId }).then((res) => {
        this.setStatusRes(res);
      });
    }

    /**
     * 获取计划详情
     *
     * @memberof TestPlanStore
     */
    loadPlanDetail(planId = this.getCurrentPlanId) {
      getPlanDetail(planId).then((res) => {
        this.setPlanInfo(res);
      });
    }

    executesAssignTo(assignToUserId) {
      return executesAssignTo(Object.keys(toJS(this.checkIdMap)), assignToUserId).then((res) => {
        this.loadExecutes();
      });
    }
}

export default new TestPlanStore();
