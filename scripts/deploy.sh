#!/usr/bin/env sh

# ------------------------------------------------------------------------------
# gh-pages 部署脚本
# @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
# @since 2020/2/10
# ------------------------------------------------------------------------------

# 装载其它库
ROOT_DIR=$(
  cd $(dirname $0)/..
  pwd
)

# 确保脚本抛出遇到的错误
set -e

# 生成静态文件
npm run build

# 进入生成的文件夹
cd ${ROOT_DIR}/docs/.temp

# 如果是发布到自定义域名
# echo 'www.example.com' > CNAME

if [[ ${GITHUB_TOKEN} && ${GITEE_TOKEN} ]]; then
  msg='自动部署'
  GITHUB_URL=https://dunwu:${GITHUB_TOKEN}@github.com/dunwu/db-tutorial.git
  GITEE_URL=https://turnon:${GITEE_TOKEN}@gitee.com/turnon/db-tutorial.git
  git config --global user.name "dunwu"
  git config --global user.email "forbreak@163.com"
else
  msg='手动部署'
  GITHUB_URL=git@github.com:dunwu/db-tutorial.git
  GITEE_URL=git@gitee.com:turnon/db-tutorial.git
fi
git init
git add -A
git commit -m "${msg}"
# 推送到github gh-pages分支
git push -f "${GITHUB_URL}" master:gh-pages
git push -f "${GITEE_URL}" master:gh-pages

cd -
rm -rf ${ROOT_DIR}/docs/.temp
