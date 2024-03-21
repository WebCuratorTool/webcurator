import { describe, it, expect } from 'vitest'

import { mount } from '@vue/test-utils'
import PageHeader from '../PageHeader.vue'

describe('PageHeader', () => {
  it('renders properly', () => {
    const wrapper = mount(PageHeader, { props: { title: 'Test title' } })
    expect(wrapper.text()).toContain('Test title')
  })
})
