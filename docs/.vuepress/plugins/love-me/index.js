const path = require('path')
const LoveMyPlugin = (options = {}) => ({
  define() {
    const COLOR =
      options.color ||
      'rgb(' + ~~(255 * Math.random()) + ',' + ~~(255 * Math.random()) + ',' + ~~(255 * Math.random()) + ')'
    const EXCLUDECLASS = options.excludeClassName || ''
    return { COLOR, EXCLUDECLASS }
  },
  enhanceAppFiles: [path.resolve(__dirname, 'love-me.js')]
})
module.exports = LoveMyPlugin
