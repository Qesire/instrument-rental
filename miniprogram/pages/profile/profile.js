const api = require('../../utils/api');
const app = getApp();

Page({
  data: {
    userInfo: null,
    isLoggedIn: false
  },

  onShow() {
    this.checkLoginStatus();
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (token) {
      this.setData({ isLoggedIn: true, userInfo: app.globalData.userInfo });
      // 获取最新用户信息
      api.get('/api/auth/me').then(userInfo => {
        this.setData({ userInfo });
        app.globalData.userInfo = userInfo;
      }).catch(() => {
        // token 可能已过期
      });
    } else {
      this.setData({ isLoggedIn: false, userInfo: null });
    }
  },

  onWechatLogin() {
    wx.getUserProfile({
      desc: '用于完善个人资料',
      success: (res) => {
        const userInfo = res.userInfo;
        // 先获取微信登录 code
        wx.login({
          success: (loginRes) => {
            if (loginRes.code) {
              api.post('/api/auth/wechat/login', {
                code: loginRes.code,
                nickname: userInfo.nickName,
                avatarUrl: userInfo.avatarUrl
              }).then(data => {
                wx.setStorageSync('token', data.token);
                this.setData({ isLoggedIn: true, userInfo: data.user });
                app.globalData.userInfo = data.user;
                wx.showToast({ title: '登录成功', icon: 'success' });
              });
            }
          }
        });
      },
      fail: () => {
        wx.showToast({ title: '登录取消', icon: 'none' });
      }
    });
  },

  onPhoneLogin() {
    // 手机号登录（简化处理）
    wx.navigateTo({ url: '/pages/login/login' });
  },

  onLogout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.removeStorageSync('token');
          this.setData({ isLoggedIn: false, userInfo: null });
          app.globalData.userInfo = null;
          wx.showToast({ title: '已退出', icon: 'success' });
        }
      }
    });
  },

  onMenuItemTap(e) {
    const type = e.currentTarget.dataset.type;
    switch (type) {
      case 'myReservations':
        wx.switchTab({ url: '/pages/reservations/reservations' });
        break;
      case 'about':
        wx.showModal({
          title: '关于',
          content: '乐器租赁小程序 v1.0.0',
          showCancel: false
        });
        break;
      default:
        wx.showToast({ title: '功能开发中', icon: 'none' });
    }
  }
});