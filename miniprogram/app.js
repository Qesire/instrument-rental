App({
  onLaunch() {
    // 检查登录态
    const token = wx.getStorageSync('token');
    if (token) {
      // 校验 token 有效性
      this.checkLoginStatus(token);
    }
  },

  checkLoginStatus(token) {
    wx.request({
      url: 'https://api.example.com/api/auth/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.data.code === 200) {
          this.globalData.userInfo = res.data.data;
        } else {
          wx.removeStorageSync('token');
        }
      },
      fail: () => {
        console.log('网络请求失败，使用缓存数据');
      }
    });
  },

  globalData: {
    userInfo: null
  }
});