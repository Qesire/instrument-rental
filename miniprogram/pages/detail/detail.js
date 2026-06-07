const api = require('../../utils/api');

Page({
  data: {
    model: null,
    currentImageIndex: 0,
    startDate: '',
    endDate: '',
    quantity: 1,
    quote: null,
    quoteLoading: false,
    submitting: false
  },

  onLoad(options) {
    const modelId = options.id;
    if (!modelId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      return;
    }
    this.setData({ modelId });
    this.loadModelDetail(modelId);
  },

  loadModelDetail(modelId) {
    // 详情从列表数据传入或通过接口获取
    // 简化：通过 models API 获取第一个匹配项
    api.get(`/api/models?page=0&size=100`).then(res => {
      const models = res && res.content ? res.content : [];
      const model = models.find(m => m.id == modelId);
      if (model) {
        this.setData({ model });
      } else {
        wx.showToast({ title: '乐器不存在', icon: 'none' });
      }
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  onSwiperChange(e) {
    this.setData({ currentImageIndex: e.detail.current });
  },

  onStartDateChange(e) {
    this.setData({ startDate: e.detail.value });
  },

  onEndDateChange(e) {
    this.setData({ endDate: e.detail.value });
  },

  onQuantityMinus() {
    if (this.data.quantity > 1) {
      this.setData({ quantity: this.data.quantity - 1 });
    }
  },

  onQuantityPlus() {
    this.setData({ quantity: this.data.quantity + 1 });
  },

  onGetQuote() {
    const { modelId, startDate, endDate, quantity } = this.data;

    if (!startDate || !endDate) {
      wx.showToast({ title: '请选择租赁日期', icon: 'none' });
      return;
    }

    if (endDate <= startDate) {
      wx.showToast({ title: '结束日期必须晚于开始日期', icon: 'none' });
      return;
    }

    this.setData({ quoteLoading: true });

    const startTime = startDate + 'T00:00:00';
    const endTime = endDate + 'T00:00:00';

    api.post('/api/reservations/quote', {
      modelId: Number(modelId),
      startTime,
      endTime,
      quantity
    }).then(quote => {
      this.setData({ quote });
    }).finally(() => {
      this.setData({ quoteLoading: false });
    });
  },

  onReserveNow() {
    if (!this.data.quote) {
      wx.showToast({ title: '请先获取报价', icon: 'none' });
      return;
    }

    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' });
      return;
    }

    const { modelId, startDate, endDate, quantity } = this.data;
    this.setData({ submitting: true });

    api.post('/api/reservations', {
      modelId: Number(modelId),
      startTime: startDate + 'T00:00:00',
      endTime: endDate + 'T00:00:00',
      quantity
    }).then(reservations => {
      wx.showToast({ title: '预约成功', icon: 'success' });
      setTimeout(() => {
        wx.switchTab({ url: '/pages/reservations/reservations' });
      }, 1500);
    }).finally(() => {
      this.setData({ submitting: false });
    });
  }
});