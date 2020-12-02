import _ from 'lodash';
import axios from 'axios';
let config = require('../../config');

let backendConfigurer = function () {
    switch (process.env.NODE_ENV) {
        case 'testing':
        case 'development':
        return 'http://' + config.dev.backendHost + ':' + config.dev.backendPort;
        case 'production':
        return 'https://' + config.build.backendHost + ':' + config.build.backendPort;
    }
}

let backendUrl = backendConfigurer();

let AXIOS = axios.create({
    baseURL: backendUrl
    // headers: {'Access-Control-Allow-Origin': frontendUrl}
});

export default {
    name: 'eventregistration',
    data() {
        return {
            // modified this
            persons: [],
            events: [],
            circuses: [],
            organizers: [],
            newPerson: '',
            deviceID:'',
            paymentAmount:'',
            personType: ['Person', 'Organizer'],
            newEvent: {
                name: '',
                date: '2017-12-08',
                startTime: '09:00',
                endTime: '11:00',
                company:'',
            },
            selectedPerson: '',
            selectedOrganizer:'',
            selectedEvent: '',
            selectedPersonType:'',
            errorPerson: '',
            errorOrganizer:'',
            errorEvent: '',
            errorRegistration: '',
            errorPayment:'',
            response: [],
        }
    },
    created: function () {
        // Initializing persons
        AXIOS.get('/persons')
        .then(response => {
            this.persons = response.data;
            this.persons.forEach(person => this.getRegistrations(person.name))
        })
        .catch(e => {this.errorPerson = e});

        AXIOS.get('/events')
        .then(response => {
            this.events = response.data
        })
        .catch(e => {this.errorEvent = e});

        AXIOS.get('/circuses')
        .then(resp => {
            this.circuses = resp.data;
        })
        .catch(e => {this.errorCircus = e});

        AXIOS.get('/organizers')
        .then(response => {
            this.organizers = response.data;
        })
        .catch(e => {this.errorOrganizer = e});

    },

    methods: {

        createPerson: function (personType, personName) {
            if(personType === "Organizer"){
                AXIOS.post('/organizer/'.concat(personName), {}, {})
                .then(response => {
                    console.log(response.data);
                    this.persons.push(response.data);
                    this.organizers.push(response.data);
                    this.errorPerson = '';
                    this.errorOrganizer='';
                    this.newPerson = '';
                })
                .catch(e => {
                    e = e.response.data.message ? e.response.data.message : e;
                    this.errorOrganizer = e;
                    console.log(e);
                });
            } else  {
                AXIOS.post('/persons/'.concat(personName), {}, {})
                .then(response => {
                    console.log(response.data);
                    this.persons.push(response.data);
                    this.errorPerson = '';
                    this.newPerson = '';

                })
                .catch(e => {
                    e = e.response.data.message ? e.response.data.message : e;
                    this.errorPerson = e;
                    console.log(e);
                });
            }

        },

        createEvent: function (newEvent) {
            let url = '';
            AXIOS.post('/events/'.concat(newEvent.name), {}, {params: newEvent})
            .then(response => {
                console.log(response.data);
                this.events.push(response.data);
                this.errorEvent = '';
                this.newEvent.name = this.newEvent.make = this.newEvent.movie = this.newEvent.company = this.newEvent.artist = this.newEvent.title = '';
            })
            .catch(e => {
                e = e.response.data.message ? e.response.data.message : e;
                this.errorEvent = e;
                console.log(e);
            });
        },

        registerEvent: function (personName, eventName) {
            let event = this.events.find(x => x.name === eventName);
            let person = this.persons.find(x => x.name === personName);
            let params = {
                person: person.name,
                event: event.name
            };

            AXIOS.post('/register', {}, {params: params})
            .then(response => {
                console.log(response.data);
                person.eventsAttended.push(event);
                this.selectedPerson = '';
                this.selectedEvent = '';
                this.errorRegistration = '';
            })
            .catch(e => {
                e = e.response.data.message ? e.response.data.message : e;
                this.errorRegistration = e;
                console.log(e);
            });
        },

        getRegistrations: function (personName) {
            AXIOS.get('/events/person/'.concat(personName))
            .then(response => {
                if (!response.data || response.data.length <= 0) return;

                let indexPart = this.persons.map(x => x.name).indexOf(personName);
                this.persons[indexPart].eventsAttended = [];
                response.data.forEach(event => {
                    let eventParams = {
                        person: personName,
                        event: event.name
                    };
                    AXIOS.get('/registrations', {params: eventParams}).then(resp => {
                        if(!resp.data || resp.data.length <= 0) return;
                        console.log(resp.data);
                        let rDto = resp.data;
                        if(rDto.payment){
                            let deviceID = rDto.payment.deviceID;
                            let paymentAmount = rDto.payment.amount;
                            if(deviceID && paymentAmount){
                                event.deviceID = deviceID;
                                event.paymentAmount = paymentAmount;
                                console.log(event);
                            }
                        }})
                        .then(() => {
                            this.persons[indexPart].eventsAttended.push(event);
                        });
                    });
                })
                .catch(e => {
                    e = e.response.data.message ? e.response.data.message : e;
                    console.log(e);
                });

            },

            assignOrganizer: function(selectedOrganizer, selectedEvent){
                let event = this.events.find(x => x.name === selectedEvent);
                let person = this.persons.find(x => x.name === selectedOrganizer);
                let params = {
                    organizer: person.name,
                    event: event.name
                };

                AXIOS.post('/organize', {}, {params: params})
                .then(response => {
                    console.log(response.data);
                    person.eventsAttended.push(event)
                    console.log(person.eventsAttended);
                    this.selectedOrganizer = '';
                    this.selectedEvent = '';
                    this.errorOrganizer = '';
                })
                .catch(e => {
                    e = e.response.data.message ? e.response.data.message : e;
                    this.errorOrganizer = e;
                    console.log(e);
                });
            },

            pay: async function(deviceID, paymentAmount, selectedEvent, selectedPerson) {
                let params = {};
                params.person = selectedPerson;
                params.event = selectedEvent;
                params.deviceID = deviceID;
                params.paymentAmount = paymentAmount;

                AXIOS.post('/pay', {}, {params: params}).then(response => {
                    if(!response.data ||response.data.length <=0) return;
                    console.log(response.data);
                    this.errorPayment='';
                    // update event.deviceID and event.paymentAmount
                    this.getRegistrations(params.person);

                    this.deviceID='';
                    this.paymentAmount='';
                    this.selectedPerson='';
                    this.selectedEvent='';
                })
                .catch(e => {
                    e = e.response.data.message ? e.response.data.message : e;
                    this.errorPayment = e;
                });
            },
        }
    }
