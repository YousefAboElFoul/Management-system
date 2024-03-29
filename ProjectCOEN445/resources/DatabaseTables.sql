DROP TABLE IF EXISTS acceptmessage,addmessage,cancelmessage,confirmmessage,invitemessage,notscheduledmessage,
    participantsconfirmed,rejectmessage,requestmessage,responsemessage,roomchangemessage,
    roomreservation,scheduledmessage,withdrawmessage,messagecount,registration,bookings;


CREATE TABLE RequestMessage(
                               ID SERIAL PRIMARY KEY NOT NULL,
                               REQUESTNUMBER VARCHAR(255) NOT NULL,
                               DATEINSERTED date NOT NULL,
                               CURRENTTIME time NOT NULL,
                               MINIMUM int NOT NULL,
                               LISTOFPARTICIPANTS VARCHAR(255) NOT NULL,
                               TOPIC VARCHAR(255) NOT NULL,
                               CONSTRAINT Request_uq UNIQUE (REQUESTNUMBER)
);

CREATE TABLE ResponseMessage(
                                ID SERIAL PRIMARY KEY NOT NULL,
                                REQUESTNUMBER VARCHAR(255) NOT NULL,
                                CONSTRAINT Response_uq UNIQUE (REQUESTNUMBER),
                                CONSTRAINT Response_fk
                                    FOREIGN KEY (REQUESTNUMBER) REFERENCES RequestMessage (REQUESTNUMBER)
);

-- Could update roomnumber and/or meetingnumber
CREATE TABLE RoomReservation(
                                ID SERIAL PRIMARY KEY NOT NULL,
                                ROOMNUMBER VARCHAR(255) NOT NULL,
                                DATEINSERTED date NOT NULL,
                                START_TIME time NOT NULL,
                                MEETINGNUMBER VARCHAR(255) NOT NULL,
                                CONSTRAINT Room_uk UNIQUE (MEETINGNUMBER, ROOMNUMBER),
                                CONSTRAINT RoomU_uk UNIQUE (ROOMNUMBER, DATEINSERTED, START_TIME)
);



CREATE TABLE Registration(
                             ID SERIAL PRIMARY KEY NOT NULL,
                             CLIENTNAME VARCHAR(255) NOT NULL,
                             IPADDRESS VARCHAR(255) NOT NULL,
                             LISTENINGPORT INT DEFAULT 44445,
                             CONSTRAINT Registration_c_uk UNIQUE (CLIENTNAME),
                             CONSTRAINT Registration_i_uk UNIQUE (IPADDRESS)
);

CREATE TABLE InviteMessage(
                              ID SERIAL PRIMARY KEY NOT NULL,
                              MEETINGNUMBER VARCHAR(255) NOT NULL,
                              DATEINSERTED date NOT NULL,
                              MEETINGTIME time NOT NULL,
                              TOPIC VARCHAR(255) NOT NULL,
                              REQUESTER VARCHAR(255) NOT NULL,
                              REQUESTNUMBER VARCHAR(255) NOT NULL,
                              CONSTRAINT Invite_uq UNIQUE (MEETINGNUMBER),
                              CONSTRAINT Invite_Reg_fk
                                  FOREIGN KEY (REQUESTER) REFERENCES Registration (CLIENTNAME) ON UPDATE CASCADE
);

CREATE TABLE AcceptMessage(
                              ID SERIAL PRIMARY KEY NOT NULL,
                              MEETINGNUMBER VARCHAR(255) NOT NULL,
                              WHOACCEPTED VARCHAR(255) NOT NULL,
                              CONSTRAINT Accept_uq UNIQUE (MEETINGNUMBER, WHOACCEPTED),
                              CONSTRAINT Accept_fk
                                  FOREIGN KEY (MEETINGNUMBER) REFERENCES InviteMessage (MEETINGNUMBER),
                              CONSTRAINT Accept_Reg_fk
                                  FOREIGN KEY (WHOACCEPTED) REFERENCES Registration (CLIENTNAME) ON UPDATE CASCADE
);

CREATE TABLE RejectMessage(
                              ID SERIAL PRIMARY KEY NOT NULL,
                              MEETINGNUMBER VARCHAR(255) NOT NULL,
                              WHOREJECTED VARCHAR(255) NOT NULL,
                              CONSTRAINT Reject_uq UNIQUE (MEETINGNUMBER, WHOREJECTED),
                              CONSTRAINT Reject_Inv_fk
                                  FOREIGN KEY (MEETINGNUMBER) REFERENCES InviteMessage (MEETINGNUMBER),
                              CONSTRAINT Reject_Reg_fk
                                  FOREIGN KEY (WHOREJECTED) REFERENCES Registration (CLIENTNAME) ON UPDATE CASCADE
);

CREATE TABLE ConfirmMessage(
                               ID SERIAL PRIMARY KEY NOT NULL,
                               MEETINGNUMBER VARCHAR(255) NOT NULL,
                               ROOMNUMBER VARCHAR(255) NOT NULL,
                               CONSTRAINT Confirm_uq UNIQUE (MEETINGNUMBER),
                               CONSTRAINT Confirm_Room_fk
                                   FOREIGN KEY (MEETINGNUMBER, ROOMNUMBER) REFERENCES RoomReservation (MEETINGNUMBER, ROOMNUMBER)
                                       ON DELETE CASCADE ON UPDATE CASCADE

);

-- Could update list confirmed participants
CREATE TABLE ScheduledMessage(
                                 ID SERIAL PRIMARY KEY NOT NULL,
                                 REQUESTNUMBER VARCHAR(255) NOT NULL,
                                 MEETINGNUMBER VARCHAR(255) NOT NULL,
                                 ROOMNUMBER VARCHAR(255) NOT NULL,
                                 LISTOFCONFIRMEDPARTICIPANTS VARCHAR(255) DEFAULT NULL,
                                 CONSTRAINT Scheduled_uq UNIQUE (MEETINGNUMBER),
                                 CONSTRAINT Scheduled_Req_fk
                                     FOREIGN KEY (REQUESTNUMBER) REFERENCES RequestMessage (REQUESTNUMBER),
                                 CONSTRAINT Scheduled_Room_fk
                                     FOREIGN KEY (MEETINGNUMBER, ROOMNUMBER) REFERENCES RoomReservation (MEETINGNUMBER ,ROOMNUMBER)
                                         ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE CancelMessage(
                              ID SERIAL PRIMARY KEY NOT NULL,
                              MEETINGNUMBER VARCHAR(255) NOT NULL,
                              WHOCANCELED VARCHAR(255) NOT NULL,
                              CONSTRAINT Cancel_uq UNIQUE (MEETINGNUMBER, WHOCANCELED)
);

-- Could update list confirmed participants
CREATE TABLE NotScheduledMessage(
                                    ID SERIAL PRIMARY KEY NOT NULL,
                                    REQUESTNUMBER VARCHAR(255) NOT NULL,
                                    DATEINSERTED date NOT NULL,
                                    PROPOSEDTIME time NOT NULL,
                                    MINIMUM int NOT NULL,
                                    LISTOFCONFIRMEDPARTICIPANTS VARCHAR(255) DEFAULT NULL,
                                    TOPIC VARCHAR(255) NOT NULL,
                                    CONSTRAINT NotScheduled_uq UNIQUE (REQUESTNUMBER, DATEINSERTED, PROPOSEDTIME, MINIMUM, TOPIC),
                                    CONSTRAINT NotScheduled_Req_fk
                                        FOREIGN KEY (REQUESTNUMBER) REFERENCES RequestMessage (REQUESTNUMBER)
);

CREATE TABLE WithdrawMessage(
                                ID SERIAL PRIMARY KEY NOT NULL,
                                MEETINGNUMBER VARCHAR(255) NOT NULL,
                                WHOWITHDRAWED VARCHAR(255) NOT NULL,
                                CONSTRAINT Withdraw_uq UNIQUE (MEETINGNUMBER, WHOWITHDRAWED),
                                CONSTRAINT Withdraw_Inv_fk
                                    FOREIGN KEY (MEETINGNUMBER) REFERENCES ScheduledMessage (MEETINGNUMBER) ON DELETE CASCADE,
                                CONSTRAINT Withdraw_Reg_fk
                                    FOREIGN KEY (WHOWITHDRAWED) REFERENCES Registration (CLIENTNAME) ON UPDATE CASCADE
);

-- Could update wasadded
CREATE TABLE AddMessage(
                           ID SERIAL PRIMARY KEY NOT NULL,
                           MEETINGNUMBER VARCHAR(255) NOT NULL,
                           WASADDED BOOLEAN DEFAULT FALSE,
                           WHO VARCHAR(255) NOT NULL,
                           CONSTRAINT Add_uq UNIQUE (MEETINGNUMBER),
                           CONSTRAINT Add_Rej_fk
                               FOREIGN KEY (MEETINGNUMBER, WHO) REFERENCES RejectMessage (MEETINGNUMBER, WHOREJECTED)
);

CREATE TABLE RoomChangeMessage(
                                  ID SERIAL PRIMARY KEY NOT NULL,
                                  MEETINGNUMBER VARCHAR(255) NOT NULL,
                                  NEWROOM  VARCHAR(255) NOT NULL,
                                  CONSTRAINT RoomChange_uq UNIQUE (MEETINGNUMBER, NEWROOM),
                                  CONSTRAINT RoomChange_Inv_fk
                                      FOREIGN KEY (MEETINGNUMBER) REFERENCES InviteMessage (MEETINGNUMBER)
);

-- Could update confirmed
CREATE TABLE ParticipantsConfirmed(
                                      ID SERIAL PRIMARY KEY NOT NULL,
                                      MEETINGNUMBER VARCHAR(255) NOT NULL,
                                      WHO VARCHAR(255) NOT NULL,
                                      CONFIRMED BOOLEAN DEFAULT TRUE,
                                      CONSTRAINT PartConf_uq UNIQUE (MEETINGNUMBER, WHO),
                                      CONSTRAINT PartConf_Inv_fk
                                          FOREIGN KEY (MEETINGNUMBER) REFERENCES InviteMessage (MEETINGNUMBER),
                                      CONSTRAINT PartConf_Reg_fk
                                          FOREIGN KEY (WHO) REFERENCES Registration (CLIENTNAME) ON UPDATE CASCADE
);

CREATE TABLE MessageCount(
                             WHO VARCHAR(255) PRIMARY KEY NOT NULL,
                             MCOUNT INT DEFAULT 1
);

CREATE TABLE Bookings(
                         ID SERIAL PRIMARY KEY NOT NULL,
                         CLIENTNAME VARCHAR(255) NOT NULL,
                         MEETINGNUMBER VARCHAR(255) NOT NULL,
                         DATEINSERTED date NOT NULL,
                         START_TIME time NOT NULL,
                         ROOMNUMBER VARCHAR(255) NOT NULL,
                         CONSTRAINT Bookings_uk UNIQUE (CLIENTNAME, MEETINGNUMBER),
                         CONSTRAINT Bookings_Reg_fk
                             FOREIGN KEY (CLIENTNAME) REFERENCES Registration (CLIENTNAME) ON UPDATE CASCADE,
                         CONSTRAINT Bookings_Room_fk
                             FOREIGN KEY (MEETINGNUMBER, ROOMNUMBER) REFERENCES RoomReservation (MEETINGNUMBER, ROOMNUMBER)
                                 ON DELETE CASCADE ON UPDATE CASCADE
);