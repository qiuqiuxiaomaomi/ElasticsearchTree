package controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.MaterialSynchonizeService;


/**
 * Created by yangmingquan on 2018/9/7.
 */
@Api(value = "MaterialSynchonizeController", description = "素材同步Api")
@RestController
@RequestMapping("/materialsync")
public class MaterialSynchonizeController {
    @Autowired
    private MaterialSynchonizeService materialSynchonizeService;

    @ApiOperation(value = "素材同步接口", httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "uid", value = "素材id", required = true, dataType = "Integer", paramType = "path")
    })
    @RequestMapping(value = "/user/{uid}")
    public Object syncUser(@PathVariable Integer uid){
        try {
            materialSynchonizeService.materialSync(uid);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "ok";
    }

    @ApiOperation(value = "素材全量同步接口", httpMethod = "GET")
    @RequestMapping(value = "/importuser")
    public Object importuser(){
        try {
            materialSynchonizeService.importAll();
        }catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "ok";
    }
}
