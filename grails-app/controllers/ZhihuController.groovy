import com.alibaba.fastjson.JSONObject
import grails.converters.JSON
import groovy.sql.Sql
import org.hibernate.criterion.Order

class ZhihuController {
    def dataSource

    def index() {}

    def giveMeFive(){
        def minId = params.minId? params.getLong("minId"):Integer.MAX_VALUE
        def size = params.max? params.size:5
        def data = new Sql(dataSource)
        params.max = params.max ? params.int('max') : 5
        params.offset = 0
        def query = {
            le("id",minId)
            order("id","desc")
            eq("enabled",true)
        }
        def list = ZhihuCollectContent.createCriteria().list(params,query)
        JSONObject json = new JSONObject()
        json.put("status","1")
        if (list.size()!=0){
            json.put("nexBaseId",(list.get(list.size()-1) as  ZhihuCollectContent).id-1)
        }else{
            json.put("nexBaseId",0)
        }
        json.put("list",list)
        render json as JSON
    }

    def del = {
        def id = params.id
        def zhihu = ZhihuCollectContent.get(id)
        if (zhihu){
            zhihu.enabled = false
            zhihu.save(flush: true)
        }
        render ""
    }
/**
 * 处理知乎防盗链 http://pic4.zhimg.com/90e3e56170e63620d7cfa8afb5ce3d0b_b.jpg
 */
    def pic = {
        def url = params.url
        def out = response.outputStream
        out << new URL("http://pic4.zhimg.com/"+url).openStream()
    }
}
