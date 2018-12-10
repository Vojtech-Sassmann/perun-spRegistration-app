import {Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ConfigService} from "../../core/services/config.service";
import {ApplicationItem} from "../../core/models/ApplicationItem";
import {FormGroup} from "@angular/forms";
import {RequestsService} from "../../core/services/requests.service";
import {ApplicationItemComponent} from "./application-item/application-item.component";
import {MatSnackBar} from "@angular/material";
import {TranslateService} from "@ngx-translate/core";
import {PerunAttribute} from "../../core/models/PerunAttribute";

@Component({
  selector: 'app-new-request',
  templateUrl: './new-request.component.html',
  styleUrls: ['./new-request.component.scss']
})
export class NewRequestComponent implements OnInit {

  constructor(
    private configService: ConfigService,
    private requestsService: RequestsService,
    private snackBar: MatSnackBar,
    private translate: TranslateService) { }

  @ViewChildren(ApplicationItemComponent)
  items: QueryList<ApplicationItemComponent>;

  serviceSelected : string;

  isFormVisible = false;
  isCardBodyVisible = false;
  oidcEnabled: boolean;
  loading = true;
  selected = "";

  // translations
  errorText : string;
  successfullySubmittedText: string;

  applicationItems: ApplicationItem[];

  ngOnInit() {
    this.requestsService.login().subscribe();

    this.configService.isOidcEnabled().subscribe(isEnabled => {
      this.oidcEnabled = isEnabled;
      this.loading = false;
    });

    this.translate.get('REQUESTS.NEW_VALUES_ERROR_MESSAGE')
      .subscribe(value => this.errorText = value);
    this.translate.get('REQUESTS.SUCCESSFULLY_SUBMITTED_MESSAGE')
      .subscribe(value => this.successfullySubmittedText = value);
  }

  revealForm() {
    this.loading = false;
    this.isCardBodyVisible = true;
    this.isFormVisible = true;
  }

  onLoading() {
    this.loading = true;
    this.isCardBodyVisible = false;
  }

  oidcSelected() {
    this.onLoading();
    this.selected = "oidc";

    this.configService.getOidcApplicationItems().subscribe(items => {
      this.applicationItems = NewRequestComponent.sortItems(items);
      this.revealForm();
    });
  }

  samlSelected() {
    this.onLoading();
    this.selected = "saml";

    this.configService.getSamlApplicationItems().subscribe(items => {
      this.applicationItems = NewRequestComponent.sortItems(items);
      this.revealForm();
    })
  }

  attributesHasCorrectValues() : boolean {

    let attributeItems = this.items.toArray();

    for (const i of attributeItems) {
      if (!i.hasCorrectValue()) {
        return false;
      }
    }

    return true;
  }

  submitRequest() {
    this.items.forEach(i => i.onFormSubmitted());

    if (!this.attributesHasCorrectValues()) {
      this.snackBar.open(this.errorText, null, {duration: 6000});
      return;
    }

    let perunAttributes : PerunAttribute[] = [];

    this.items.forEach(i => {
      let attr = i.getAttribute();
      let perunAttr = new PerunAttribute(attr.value, attr.urn);
      perunAttributes.push(perunAttr);
    });

    this.requestsService.createRegistrationRequest(perunAttributes).subscribe(requestId => {
      this.snackBar.open(this.successfullySubmittedText, null, {duration: 6000});
    }, error => {
      console.log("Error");
      console.log(error);
    })
  }

  private static getItemOrderValue(item : ApplicationItem) : number {
    let value;

    switch (item.type) {
      case 'java.lang.String':
        value = 0;
        break;
      case 'java.util.ArrayList':
        if (item.allowedValues !== null) {
          value = 1;
        } else {
          value = 2;
        }
        break;
      case 'java.util.LinkedHashMap':
        value = 3;
        break;
      case 'java.lang.Boolean':
        value = 4;
        break;
      default:
        value = 5;
    }

    return value;
  }

  private static sortItems(items : ApplicationItem[]) : ApplicationItem[] {
    return items.sort(((a, b) => {
      let aValue = NewRequestComponent.getItemOrderValue(a);
      let bValue = NewRequestComponent.getItemOrderValue(b);

      return aValue - bValue;
    }))
  }
}
