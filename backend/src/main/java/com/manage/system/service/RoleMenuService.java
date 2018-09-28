package com.manage.system.service;

import com.manage.system.dao.RoleMenuMapper;
import com.manage.system.dao.RolePermMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色菜单服务类
 * Created by luya on 2018/9/14.
 */
@Service
public class RoleMenuService {

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Transactional(readOnly = true)
    public List<Integer> getMenuIdByRole(Integer roleId) throws Exception {
        List<Integer> menuIds = roleMenuMapper.selectByRoleId(roleId);
        return menuIds;
    }

    @Transactional(readOnly = true)
    public Integer getMenuIdByParam(String[] roleIds, Integer menuId) throws Exception {
        List<Integer> roleIdList = new ArrayList<>();
        for (String id : roleIds) {
            roleIdList.add(Integer.parseInt(id));
        }
        Integer id = roleMenuMapper.selectByParam(menuId, roleIdList);
        return id;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int insertRoleMenu(Integer menuId, Integer roleId) throws Exception {
        return roleMenuMapper.insert(menuId, roleId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public int deleteByPrimaryKey(Integer menuId, Integer roleId, Integer menuType) throws Exception {
        return roleMenuMapper.deleteByPrimaryKey(menuId, roleId, menuType);
    }
}