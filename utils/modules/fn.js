// 类型判断
exports.type = function (o){
  var s = Object.prototype.toString.call(o)
  return s.match(/\[object (.*?)\]/)[1].toLowerCase()
}

 // 修复date时区格式的问题
 exports.repairDate = function (date) {
  date = new Date(date);
  return `${date.getUTCFullYear()}-${zero(date.getUTCMonth()+1)}-${zero(date.getUTCDate())} ${zero(date.getUTCHours())}:${zero(date.getUTCMinutes())}:${zero(date.getUTCSeconds())}`;
}

// 日期的格式
exports.dateFormat = function (date) {
  return `${date.getFullYear()}-${zero(date.getMonth()+1)}-${zero(date.getDate())} ${zero(date.getHours())}:${zero(date.getMinutes())}:${zero(date.getSeconds())}`
}

// 小于10补0
function zero(d){
  return d.toString().padStart(2,'0')
}