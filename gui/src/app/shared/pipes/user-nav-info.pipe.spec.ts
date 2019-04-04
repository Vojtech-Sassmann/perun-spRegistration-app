import { UserFullNamePipe } from './user-nav-info.pipe';

describe('UserNavInfoPipe', () => {
  it('create an instance', () => {
    const pipe = new UserFullNamePipe();
    expect(pipe).toBeTruthy();
  });
});
