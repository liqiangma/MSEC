
/**
 * Tencent is pleased to support the open source community by making MSEC available.
 *
 * Copyright (C) 2016 THL A29 Limited, a Tencent company. All rights reserved.
 *
 * Licensed under the GNU General Public License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. You may 
 * obtain a copy of the License at
 *
 *     https://opensource.org/licenses/GPL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the 
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */


package beans.service;

import beans.dbaccess.IDL;
import beans.dbaccess.SecondLevelServiceConfigTag;
import beans.response.DelIDLTagResponse;
import beans.response.DelSecondLevelServiceConfigTagResponse;
import ngse.org.DBUtil;
import ngse.org.JsonRPCHandler;
import org.apache.log4j.Logger;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/28.
 * 删除标准服务的IDL版本
 */
public class DelIDLTag extends JsonRPCHandler {
    static private void RemoveFile(String filename) {
        File file = new File(filename);
        file.delete();


    }
    public DelIDLTagResponse exec(IDL request)
    {
        Logger logger = Logger.getLogger(this.getClass().getName());
        DelIDLTagResponse response = new DelIDLTagResponse();
        response.setMessage("unkown error.");
        response.setStatus(100);
        String result = checkIdentity();
        if (!result.equals("success"))
        {
            response.setStatus(99);
            response.setMessage(result);
            return response;
        }
        if (request.getTag_name()== null ||
                request.getTag_name().equals("") ||
                request.getSecond_level_service_name() == null||
                request.getSecond_level_service_name().equals("")||
                request.getFirst_level_service_name() == null ||
                request.getFirst_level_service_name().equals(""))
        {
            response.setMessage("tag name and service name  should NOT be empty.");
            response.setStatus(100);
            return response;
        }
        DBUtil util = new DBUtil();
        if (util.getConnection() == null)
        {
            response.setMessage("DB connect failed.");
            response.setStatus(100);
            return response;
        }
        String sql = "delete from t_idl_tag where tag_name=? and first_level_service_name=? and second_level_service_name=?";
        List<Object> params = new ArrayList<Object>();
        params.add(request.getTag_name());
        params.add(request.getFirst_level_service_name());
        params.add(request.getSecond_level_service_name());
        try {
            //删除数据库记录
            int delNum = util.updateByPreparedStatement(sql, params);
            String filename = IDL.getIDLFileName(request.getFirst_level_service_name(), request.getSecond_level_service_name(), request.getTag_name());

             //删除文件
            logger.error("delte file:"+filename);
            RemoveFile(filename);

            if (delNum > 0)
            {
                response.setMessage("success");
                response.setDelNumber(delNum);
                response.setStatus(0);
                return response;
            }
            else {
                response.setMessage("delete record number is "+delNum);
                response.setDelNumber(delNum);
                response.setStatus(100);
                return response;
            }
        }
        catch (SQLException e)
        {
            response.setMessage("Delete record failed:"+e.toString());
            response.setStatus(100);
            e.printStackTrace();
            return response;
        }
        finally {
            util.releaseConn();
        }

    }
    public static String deleteAll(String flsn, String slsn)
    {
        DBUtil util = new DBUtil();
        if (util.getConnection() == null)
        {
            return "DB connect failed.";
        }

        try {
            String sql = "select tag_name from t_idl_tag where  first_level_service_name=? and second_level_service_name=?";
            List<Object> params = new ArrayList<Object>();

            params.add(flsn);
            params.add(slsn);

            ArrayList<IDL> result = util.findMoreRefResult(sql, params, IDL.class);

            for (int i = 0; i < result.size() ; i++) {
                String tag_name = result.get(i).getTag_name();
                sql = "delete from t_idl_tag where tag_name=? and first_level_service_name=? and second_level_service_name=?";
                params = new ArrayList<Object>();
                params.add(tag_name);
                params.add(flsn);
                params.add(slsn);

                    //删除数据库记录
                int delNum = util.updateByPreparedStatement(sql, params);

                String filename = IDL.getIDLFileName(flsn, slsn, tag_name);

                RemoveFile(filename);
            }
            return "success";


        } catch (Exception e) {

            e.printStackTrace();
            return e.getMessage();
        } finally {
            util.releaseConn();
        }


    }
}