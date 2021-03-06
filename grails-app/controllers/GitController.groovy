import com.alibaba.fastjson.JSONObject
import grails.converters.JSON
import groovy.sql.Sql

class GitController {
    static allowedMethods = [pushEvent: "POST"]
    def dataSource


    def index() {
        render (view: "test")
    }
    /**
     * Git Webhooks 服务
     */
    def pushEvent = {
        String body = getBody()
        log.info "body:"+body
        JSONObject json = JSONObject.parseObject(body)
        def rep = json.repository
        def commit = json.head_commit //获取第一个commit
        if (!rep||!commit){
            render "failure"
            return
        }
        GitPush gp = new GitPush(
                repositoryId: rep.id,
                repositoryName: rep.name,
                repositoryUrl: rep.html_url,
                repositoryDesc: rep.description,
                repositoryHomePage: rep.html_url,
                commitId:commit.id,
                commitDate: commit.timestamp,
                commitUrl: commit.url,
                commitAuthorName: commit.author.name,
                commitAuthorEmail: commit.author.email,
                commitMsg: commit.message,
                dateCreated: new Date())
        if (gp.save(flush: true)){
            log.info "保存成功"
            render "success"
        }else {
            log.info "保存失败:"+gp.commitMsg
            render "save failure"
        }
    }
    /**
     * 提供json接口
     */
    def commitHistory = {
        try {
            def db = new Sql(dataSource)
            def limit = params.limit? Integer.valueOf(params.limit):10
            def sql = "SELECT commit_msg,commit_url,commit_date,repository_name,repository_url from git_push order BY commit_date DESC LIMIT :limit"
            def list = db.rows(sql,['limit':limit])
            render list as JSON
        }catch (Exception e){
            e.printStackTrace()
        }
        render ""
    }

    /**
     * 获取request头的payload信息
     * @return
     */
    def getBody(){
        String body = "";
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }
}
