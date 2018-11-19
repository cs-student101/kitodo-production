--
-- Remove old indexes of prior Goobi.Production CE 1.11.2
--
ALTER TABLE `batchesprozesse`
  DROP KEY `FK4614E1D551BB26FA`,
  DROP KEY `FK4614E1D58DC81D49`;

ALTER TABLE `benutzer`
  DROP KEY `FK6564F1FDAB2826EF`;

ALTER TABLE `benutzereigenschaften`
  DROP KEY `FK963DAE0FC44F7B5B`;

ALTER TABLE `benutzergruppenmitgliedschaft`
  DROP KEY `FK45CBE578C7DF00F`,
  DROP KEY `FK45CBE578C44F7B5B`;

ALTER TABLE `history`
  DROP KEY `FK373FE49436A1007C`;

ALTER TABLE `projectfilegroups`
  DROP KEY `FK51AAC2292DFE45A`;

ALTER TABLE `projektbenutzer`
  DROP KEY `FKEC749D0E2DFE45A`,
  DROP KEY `FKEC749D0EC44F7B5B`;

ALTER TABLE `prozesse`
  DROP KEY `FKC55ACC6D2DFE45A`,
  DROP KEY `FKC55ACC6DE81D30E7`,
  DROP KEY `FKC55ACC6DC729A7E5`;

ALTER TABLE `prozesseeigenschaften`
  DROP KEY `FK3B22499F51BB26FA`;

ALTER TABLE `schritte`
  DROP KEY `FKD720073651BB26FA`,
  DROP KEY `FKD720073697089D42`;

ALTER TABLE `schritteberechtigtebenutzer`
  DROP KEY `FK4BB889CF8BD09B9A`,
  DROP KEY `FK4BB889CFC44F7B5B`;

ALTER TABLE `schritteberechtigtegruppen`
  DROP KEY `FKA5A0CC818BD09B9A`,
  DROP KEY `FKA5A0CC81C7DF00F`;

ALTER TABLE `vorlagen`
  DROP KEY `FK9A46688251BB26FA`;

ALTER TABLE `vorlageneigenschaften`
  DROP KEY `FKAA25B7AAD29AC443`;

ALTER TABLE `werkstuecke`
  DROP KEY `FK98DED74551BB26FA`;

ALTER TABLE `werkstueckeeigenschaften`
  DROP KEY `FK7B209DC7FBCBC046`;