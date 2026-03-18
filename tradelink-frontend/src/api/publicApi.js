import api from './axiosInstance'

export default {
  getFaq() {
    return api.get('/public/faq')
  },
  getPlanos() {
    return api.get('/public/planos')
  }
}
