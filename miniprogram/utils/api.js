const BASE_URL = 'https://api.example.com';

/**
 * 封装微信请求
 * @param {string} url   - 接口路径
 * @param {string} method - 请求方法
 * @param {object} data   - 请求体
 * @returns {Promise}
 */
function request(url, method = 'GET', data = {}) {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');

    wx.request({
      url: BASE_URL + url,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      },
      success(res) {
        if (res.data.code === 200) {
          resolve(res.data.data);
        } else if (res.data.code === 401) {
          // token 过期或无效，清除并跳转登录
          wx.removeStorageSync('token');
          wx.reLaunch({ url: '/pages/index/index' });
          reject(res.data);
        } else {
          wx.showToast({
            title: res.data.message || '请求失败',
            icon: 'none'
          });
          reject(res.data);
        }
      },
      fail(err) {
        wx.showToast({
          title: '网络异常，请稍后重试',
          icon: 'none'
        });
        reject(err);
      }
    });
  });
}

module.exports = {
  get: (url) => request(url, 'GET'),
  post: (url, data) => request(url, 'POST', data),
  BASE_URL
};