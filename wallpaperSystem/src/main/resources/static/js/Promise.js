//相当于重写promise
function Promise(excutor){
    //添加属性
    this.PromiseState='pending'
    this.PromiseResult=''

    //在这里保存一个callback属性用来存放onResolved和onRejected方法
    this.callbacks=[]
    const that=this


    //同步调用执行器函数
    function resolve (data) {
        if (that.PromiseState!=='pending'){
            return
        }
        //修改对象的状态。promiseState
        that.PromiseState='fulfilled'
        //设置对象结果值promiseReuslt
        that.PromiseResult=data
        //异步状态下执行成功的回调函数（这里有点回归的意思）
        that.callbacks.forEach(item=>{
            item.onResolved(data)
        })
    }
    function reject (data) {
        if (that.PromiseState!=='pending'){
            return
        }
        that.PromiseState='rejected'
        that.PromiseResult=data
        //异步状态下执行失败的回调函数
        that.callbacks.forEach(item=>{
            item.onRejected(data)
        })
    }
    //这里要执行这个执行器函数
    try{
        excutor(resolve,reject)
    }
    catch (e) {
        reject(e)//
    }


}

Promise.prototype.then=function(onResolved,onRejected){
    return new Promise((resolve,reject)=>{
        try{
            //调用回到函数，需要做对this.PromiseState做条件判断
            if (this.PromiseState=='fulfilled'){
                //获取回调函数的执行结果
                let result=onResolved(this.PromiseResult)
                if (result instanceof  Promise){
                    //如果是Promise类型的对象
                    result.then(value=>{
                        resolve(value)
                    },reason => {
                        reject(reason)
                    })
                }else{
                    //结果对象设为成功
                    resolve(result)
                }
            }
            if (this.PromiseState=='rejected'){
                onRejected(this.PromiseResult)
            }
            //异步状态下这里先执行
            if (this.PromiseState=='pending'){
                //把两个函数存储到callback属性上去
                //考虑到多次使用then，因此存储以push的方式push到数组中去
                this.callbacks.push({
                    onResolved,
                    onRejected
                })
            }
        }
        catch (e) {
            reject(e)
        }

    })

}

