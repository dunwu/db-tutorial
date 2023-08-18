/** 插入自定义html模块 (可用于插入广告模块等)
 * {
 *   homeSidebarB: htmlString, 首页侧边栏底部
 *
 *   sidebarT: htmlString, 全局左侧边栏顶部
 *   sidebarB: htmlString, 全局左侧边栏底部
 *
 *   pageT: htmlString, 全局页面顶部
 *   pageB: htmlString, 全局页面底部
 *   pageTshowMode: string, 页面顶部-显示方式：未配置默认全局；'article' => 仅文章页①； 'custom' => 仅自定义页①
 *   pageBshowMode: string, 页面底部-显示方式：未配置默认全局；'article' => 仅文章页①； 'custom' => 仅自定义页①
 *
 *   windowLB: htmlString, 全局左下角②
 *   windowRB: htmlString, 全局右下角②
 * }
 *
 * ①注：在.md文件front matter配置`article: false`的页面是自定义页，未配置的默认是文章页（首页除外）。
 * ②注：windowLB 和 windowRB：1.展示区块最大宽高200px*400px。2.请给自定义元素定一个不超过200px*400px的宽高。3.在屏幕宽度小于960px时无论如何都不会显示。
 */

module.exports = {
  // 万维广告
  // pageT: `
  //   <div class="wwads-cn wwads-horizontal page-wwads" data-id="261"></div>
  //   <style>
  //     .page-wwads{
  //       width:100%!important;
  //       min-height: 0;
  //       margin: 0;
  //     }
  //     .page-wwads .wwads-img img{
  //       width:80px!important;
  //     }
  //     .page-wwads .wwads-poweredby{
  //       width: 40px;
  //       position: absolute;
  //       right: 25px;
  //       bottom: 3px;
  //     }
  //     .wwads-content .wwads-text, .page-wwads .wwads-text{
  //       height: 100%;
  //       padding-top: 5px;
  //       display: block;
  //     }
  // </style>
  // `,
  windowRB: `
    <div class="wwads-cn wwads-vertical windowRB" data-id="261" style="max-width:160px;
    min-width: auto;min-height:auto;"></div>
    <style>
      .windowRB{ padding: 0;}
      .windowRB .wwads-img{margin-top: 10px;}
      .windowRB .wwads-content{margin: 0 10px 40px 10px;}
      .custom-html-window-rb .close-but{
        display: none;
      }
    </style>
  `,
}

// module.exports = {
//   homeSidebarB: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
//   sidebarT: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
//   sidebarB: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
//   pageT: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
//   pageB: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
//   windowLB: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
//   windowRB: `<div style="width:100%;height:100px;color:#fff;background: #eee;">自定义模块测试</div>`,
// }
