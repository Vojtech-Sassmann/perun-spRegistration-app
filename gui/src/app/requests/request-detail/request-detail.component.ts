import {Component, OnDestroy, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Subscription} from "rxjs";
import {RequestsService} from "../../core/services/requests.service";
import {Request} from "../../core/models/Request";

@Component({
  selector: 'app-request-detail',
  templateUrl: './request-detail.component.html',
  styleUrls: ['./request-detail.component.scss']
})
export class RequestDetailComponent implements OnInit, OnDestroy {

  constructor(
    private route: ActivatedRoute,
    private requestsService: RequestsService
  ) { }

  private sub : Subscription;
  //TODO edit datatype
  requestItems: any[];
  displayedColumns: string[] = ['name', 'value'];

  loading = true;
  request: Request;

  private mapAttributes() {
    this.requestItems = [];
    for (let urn of Object.keys(this.request.attributes)) {
      let item = this.request.attributes[urn];
      this.requestItems.push({
          "urn" : urn,
          "value" : item.value,
          "oldValue" : item.oldValue,
          "name" : item.definition.displayName
        });
    }
  }

  ngOnInit() {
    this.sub = this.route.params.subscribe(params => {
      this.requestsService.getRequest(params['id']).subscribe(request => {
        this.request = request;
        this.loading = false;
        this.mapAttributes();
      });
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
