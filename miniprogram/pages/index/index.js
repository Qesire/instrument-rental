const api = require('../../utils/api');

Page({
  data: {
    categories: [],
    recommendations: [],
    banners: [
      { id: 1, image: '', title: '新用户首租7折' },
      { id: 2, image: '', title: '钢琴租赁月卡上线' }
    ]
  },

  onLoad() {
    this.loadCategories();
    this.loadRecommendations();
  },

  onPullDownRefresh() {
    Promise.all([
      this.loadCategories(),
      this.loadRecommendations()
    ]).finally(() => wx.stopPullDownRefresh());
  },

  loadCategories() {
    return api.get('/api/categories').then(categories => {
      this.setData({ categories });
    }).catch(() => {
      // 静默失败，首页可降级
    });
  },

  loadRecommendations() {
    return api.get('/api/models?page=0&size=6').then(res => {
      this.setData({ recommendations: (res && res.content) || [] });
    }).catch(() => {
      // 静默失败
    });
  },

  onCategoryTap(e) {
    const categoryId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/list/list?category=${categoryId}`
    });
  },

  onModelTap(e) {
    const modelId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${modelId}`
    });
  },

  onSearchTap() {
    wx.navigateTo({
      url: '/pages/list/list'
    });
  }
});