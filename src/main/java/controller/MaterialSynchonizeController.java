package controller;

import com.karakal.commons.util.ControllerUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Created by yangmingquan on 2018/9/7.
 */
@Api(value = "MaterialSynchonizeController", description = "素材同步Api")
@RestController
@RequestMapping("/materialsync")
public class MaterialSynchonizeController {

    @ApiOperation(value = "素材同步接口", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "素材id", required = true, dataType = "Integer", paramType = "path")
    })
    @RequestMapping(value = "/user/{uid}")
    public Object syncUser(@PathVariable Integer uid){
        Map<String, Object> result = ControllerUtil.defaultSuccResult();
        try {

        } catch (Exception e) {
            e.printStackTrace();
            result = ControllerUtil.defaultErrResult();
        }
        return result;
    }
}
