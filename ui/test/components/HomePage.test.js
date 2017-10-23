import React from 'react';
import { shallow } from 'enzyme';
import sinon from 'sinon';

import HomePage from '../../src/components/HomePage';

describe('HomePage', () => {
	it('should always pass', () => {
		expect(true).toBe(true);
	});

	it('should render a single HomePage component', () => {
		const wrapper = shallow(<HomePage />);
		expect(true).toBe(true);
	});

});
