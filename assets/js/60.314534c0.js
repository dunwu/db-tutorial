(window.webpackJsonp=window.webpackJsonp||[]).push([[60],{398:function(a,t,e){"use strict";e.r(t);var r=e(4),s=Object(r.a)({},(function(){var a=this,t=a._self._c;return t("ContentSlotsDistributor",{attrs:{"slot-key":a.$parent.slotKey}},[t("h1",{attrs:{id:"hbase-java-api-高级特性之协处理器"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#hbase-java-api-高级特性之协处理器"}},[a._v("#")]),a._v(" HBase Java API 高级特性之协处理器")]),a._v(" "),t("h2",{attrs:{id:"简述"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#简述"}},[a._v("#")]),a._v(" 简述")]),a._v(" "),t("p",[a._v("在使用 HBase 时，如果你的数据量达到了数十亿行或数百万列，此时能否在查询中返回大量数据将受制于网络的带宽，即便网络状况允许，但是客户端的计算处理也未必能够满足要求。在这种情况下，协处理器（Coprocessors）应运而生。它允许你将业务计算代码放入在 RegionServer 的协处理器中，将处理好的数据再返回给客户端，这可以极大地降低需要传输的数据量，从而获得性能上的提升。同时协处理器也允许用户扩展实现 HBase 目前所不具备的功能，如权限校验、二级索引、完整性约束等。")]),a._v(" "),t("h2",{attrs:{id:"参考资料"}},[t("a",{staticClass:"header-anchor",attrs:{href:"#参考资料"}},[a._v("#")]),a._v(" 参考资料")]),a._v(" "),t("ul",[t("li",[t("a",{attrs:{href:"https://item.jd.com/11321037.html",target:"_blank",rel:"noopener noreferrer"}},[a._v("《HBase 权威指南》"),t("OutboundLink")],1)]),a._v(" "),t("li",[t("a",{attrs:{href:"https://github.com/larsgeorge/hbase-book",target:"_blank",rel:"noopener noreferrer"}},[a._v("《HBase 权威指南》官方源码"),t("OutboundLink")],1)])])])}),[],!1,null,null,null);t.default=s.exports}}]);