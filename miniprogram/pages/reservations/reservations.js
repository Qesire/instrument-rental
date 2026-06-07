const api = require('../../utils/api');

Page({
  data: {
    reservations: [],
    page: 0,
    hasMore: true,
    loading: false,
    refreshing: false
  },

  onShow() {
    // 每次切换到该 tab 刷新列表
    this.setData({ page: 0, hasMore: true, reservations: [] });
    this.loadReservations();
  },

  onPullDownRefresh() {
    this.setData({ page: 0, hasMore: true, reservations: [], refreshing: true });
    this.loadReservations().finally(() => wx.stopPullDownRefresh());
  },

  onReachBottom() {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadReservations();
  },

  loadReservations() {
    const token = wx.getStorageSync('token');
    if (!token) {
      this.setData({ reservations: [] });
      return;
    }

    if (this.data.loading) return;
    this.setData({ loading: true });

    return api.get(`/api/reservations/my?page=${this.data.page}&size=20`).then(res => {
      const newList = res && res.content ? res.content : [];
      this.setData({
        reservations: this.data.page === 0 ? newList : [...this.data.reservations, ...newList],
        hasMore: newList.length >= 20
      });
    }).finally(() => {
      this.setData({ loading: false, refreshing: false });
    });
  },

  onCancelReservation(e) {
    const reservationId = e.currentTarget.dataset.id;
    const status = e.currentTarget.dataset.status;

    if (status === 'CANCELLED' || status === 'RETURNED' || status === 'EXPIRED') {
      return;
    }

    wx.showModal({
      title: '确认取消',
      content: '确定要取消该预约吗？',
      success: (res) => {
        if (res.confirm) {
          this.doCancel(reservationId);
        }
      }
    });
  },

  doCancel(reservationId) {
    api.post(`/api/reservations/${reservationId}/cancel`).then(() => {
      wx.showToast({ title: '已取消', icon: 'success' });
      // 刷新列表
      this.setData({ page: 0, hasMore: true, reservations: [] });
      this.loadReservations();
    });
  },

  getStatusLabel(status) {
    const labels = {
      'UNPAID': '待支付',
      'RESERVED': '已预约',
      'RENTED': '租赁中',
      'RETURNED': '已归还',
      'CANCELLED': '已取消',
      'EXPIRED': '已过期'
    };
    return labels[status] || status;
  },

  getStatusClass(status) {
    return 'tag tag-' + status.toLowerCase();
  },

  isCancellable(status) {
    return status === 'UNPAID' || status === 'RESERVED';
  }
});