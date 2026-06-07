const api = require('../../utils/api');

Page({
  data: {
    models: [],
    categories: [],
    activeCategory: null,
    keyword: '',
    page: 0,
    hasMore: true,
    loading: false
  },

  onLoad(options) {
    if (options.category) {
      this.setData({ activeCategory: Number(options.category) });
    }
    this.loadCategories();
    this.loadModels();
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    this.loadMore();
  },

  onPullDownRefresh() {
    this.setData({ page: 0, hasMore: true, models: [] });
    this.loadModels().finally(() => wx.stopPullDownRefresh());
  },

  loadCategories() {
    api.get('/api/categories').then(categories => {
      this.setData({ categories });
    }).catch(() => {});
  },

  onCategoryTap(e) {
    const categoryId = e.currentTarget.dataset.id;
    const newActive = this.data.activeCategory === categoryId ? null : categoryId;
    this.setData({ activeCategory: newActive, page: 0, hasMore: true, models: [] });
    this.loadModels();
  },

  onSearchInput(e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearchConfirm() {
    this.setData({ page: 0, hasMore: true, models: [] });
    this.loadModels();
  },

  onSearchClear() {
    this.setData({ keyword: '', page: 0, hasMore: true, models: [] });
    this.loadModels();
  },

  loadModels() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    const params = [];
    if (this.data.activeCategory) {
      params.push(`category=${this.data.activeCategory}`);
    }
    if (this.data.keyword) {
      params.push(`keyword=${encodeURIComponent(this.data.keyword)}`);
    }
    params.push(`page=${this.data.page}`);
    params.push('size=20');

    const queryString = params.join('&');

    return api.get(`/api/models?${queryString}`).then(res => {
      const newModels = res && res.content ? res.content : [];
      this.setData({
        models: this.data.page === 0 ? newModels : [...this.data.models, ...newModels],
        hasMore: newModels.length >= 20
      });
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  loadMore() {
    this.setData({ page: this.data.page + 1 });
    this.loadModels();
  },

  onModelTap(e) {
    const modelId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/detail/detail?id=${modelId}`
    });
  }
});