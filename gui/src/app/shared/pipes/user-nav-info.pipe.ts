import { Pipe, PipeTransform } from '@angular/core';
import {User} from "../../core/models/User";

@Pipe({
  name: 'UserFullName'
})
export class UserFullNamePipe implements PipeTransform {

  transform(value: any, args?: any): any {
    if ((<User>value).email) {
      const user = <User>value;
      let fullName = "";

      fullName = this.addValue(fullName, user.titleBefore);
      fullName = this.addValue(fullName, user.titleBefore);
      fullName = this.addValue(fullName, user.firstName);
      fullName = this.addValue(fullName, user.middleName);
      fullName = this.addValue(fullName, user.lastName);
      fullName = this.addValue(fullName, user.titleAfter);

      return fullName;
    } else {
      return value;
    }
  }

  private addValue(fullName: string, value: string): string {
    if (value.length > 0) {
      fullName += value + " ";
    }
    return fullName;
  }
}
