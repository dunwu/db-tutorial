/**
 * @see https://vuepress.vuejs.org/zh/
 */
module.exports = {
  port: "4000",
  dest: "dist",
  base: "/db-tutorial/",
  title: "DB-TUTORIAL",
  description: "数据库教程",
  head: [["link", { rel: "icon", href: `/favicon.ico` }]],
  markdown: {
    externalLinks: {
      target: "_blank",
      rel: "noopener noreferrer"
    }
  },
  themeConfig: {
    logo: "images/dunwu-logo-100.png",
    repo: "dunwu/db-tutorial",
    repoLabel: "Github",
    docsDir: 'docs',
    docsBranch: 'master',
    editLinks: true,
    smoothScroll: true,
    locales: {
      "/": {
        label: "简体中文",
        selectText: "Languages",
        editLinkText: "帮助我们改善此页面！",
        lastUpdated: "上次更新",
        nav: [
          {
            text: "sql",
            link: "/sql/",
            items: [
              {
                text: "Mysql",
                link: "/sql/mysql/"
              }
            ]
          },
          {
            text: "nosql",
            link: "/nosql/",
            items: [
              {
                text: "Redis",
                link: "/nosql/redis/"
              }
            ]
          },
          {
            text: "🎯 博客",
            link: "https://github.com/dunwu/blog",
            target: "_blank",
            rel: ""
          }
        ],
        sidebar: "auto",
        sidebarDepth: 2
      }
    }
  },
  plugins: [
    ['@vuepress/active-header-links', {
      sidebarLinkSelector: '.sidebar-link',
      headerAnchorSelector: '.header-anchor'
    }],
    ["@vuepress/back-to-top", true],
    [
      "@vuepress/pwa",
      {
        serviceWorker: true,
        updatePopup: true
      }
    ],
    ["@vuepress/medium-zoom", true],
    [
      "container",
      {
        type: "vue",
        before: '<pre class="vue-container"><code>',
        after: "</code></pre>"
      }
    ],
    [
      "container",
      {
        type: "upgrade",
        before: info => `<UpgradePath title="${info}">`,
        after: "</UpgradePath>"
      }
    ],
    ["flowchart"]
  ]
};
