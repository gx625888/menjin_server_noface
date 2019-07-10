package com.threey.guard.manage.dao;

import com.threey.guard.base.dao.CrudDAO;
import com.threey.guard.manage.domain.HouseUnit;
import org.springframework.stereotype.Repository;

@Repository
public class ManageHouseDao extends CrudDAO<HouseUnit> {
    @Override
    protected String getNameSpace() {
        return "ManageHouseSql";
    }
}
