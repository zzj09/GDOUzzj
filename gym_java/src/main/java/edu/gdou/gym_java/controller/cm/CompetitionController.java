package edu.gdou.gym_java.controller.cm;


import edu.gdou.gym_java.entity.VO.TimeLimit;
import edu.gdou.gym_java.entity.bean.ResponseBean;
import edu.gdou.gym_java.service.FieldService;
import edu.gdou.gym_java.service.UserService;
import edu.gdou.gym_java.service.cm.CompetitionCancelService;
import edu.gdou.gym_java.service.cm.CompetitionService;
import edu.gdou.gym_java.utils.TimeUtils;
import lombok.val;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 赛事表 前端控制器
 * </p>
 *
 * @author liuyuanfeng
 * @since 2022-06-04
 */
@RestController
@RequestMapping("/competition")
public class CompetitionController {
    private final UserService userService;
    private final CompetitionService competitionService;
    private final CompetitionCancelService cancelService;
    private final FieldService fieldService;
    private final Environment environment;


    public CompetitionController(UserService userService, CompetitionService competitionService, CompetitionCancelService cancelService, FieldService fieldService, Environment environment) {
        this.userService = userService;
        this.competitionService = competitionService;
        this.cancelService = cancelService;
        this.fieldService = fieldService;
        this.environment = environment;
    }



    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public ResponseBean createEvent(@RequestParam(value = "uid", required = false) String uid,
                                    @Valid @RequestParam("event_name") String name,
                                    @Valid @RequestParam(value = "event_time") String time,
                                    @Valid @RequestParam("event_length") Integer length,
                                    @Valid @RequestParam("money") String money,
                                    @RequestParam("context") String context) {
        val user = userService.currentUser();
        int uid_int;
        if ((("AM").equals(user.getRole().getInfo()) || ("SM").equals(user.getRole().getInfo()))&&uid!=null) {
            uid_int = Integer.parseInt(uid);
        } else {
            uid_int = user.getId();
        }
        val timeStamp = TimeUtils.StringToTimeStamp(time);
        val cid = competitionService.createEvent(uid_int, name, timeStamp, length, Double.valueOf(money), context);
        return new ResponseBean(200, "赛事审核id信息", cid);
    }

    @RequestMapping(value = "/cancel", method = RequestMethod.POST)
    public ResponseBean cancelEvent(@RequestParam("cid") String cid,
                                    @RequestParam(value = "uid", required = false) String uid,
                                    @RequestParam("context") String context) {
        val user = userService.currentUser();
        int uid_int;
        if ((("AM").equals(user.getRole().getInfo()) || ("SM").equals(user.getRole().getInfo()))&&uid!=null) {
            uid_int = Integer.parseInt(uid);
        } else {
            uid_int = user.getId();
        }
        int cid_int = Integer.parseInt(cid);
        val cancelEvent = competitionService.cancelEvent(cid_int, uid_int, context);
        if (cancelEvent==null){
            return new ResponseBean(200, "赛事信息不存在", null);
        }
        return new ResponseBean(200, cancelEvent?"取消成功":"取消失败", cancelEvent);
    }

    @RequestMapping(value = "/queryEvent", method = RequestMethod.GET)
    public ResponseBean queryEvents(@RequestParam(value = "cid",required = false) Integer cid,
                                    @RequestParam(value = "name", required = false) String name,
                                    @RequestParam(value = "uname",required = false) String uname,
                                    @RequestParam(value = "start_time",required = false) String start,
                                    @RequestParam(value = "end_time",required = false) String end){
        // TODO 缺少赛事地点
        TimeLimit timeLimit =null;
        if(start!=null && end!=null){
            timeLimit = new TimeLimit(TimeUtils.StringToTimeStamp(start), TimeUtils.StringToTimeStamp(end));
        }
        val competitions = competitionService.queryEvents(cid, name, uname, timeLimit);
        return new ResponseBean(200,competitions!=null?"获取到数据":"未获取到数据",competitions);
    }



}
